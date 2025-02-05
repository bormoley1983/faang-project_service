package faang.school.projectservice.service;

import faang.school.projectservice.dto.campaign.CampaignDto;
import faang.school.projectservice.dto.campaign.CampaignFilterDto;
import faang.school.projectservice.mapper.campaign.CampaignMapper;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static faang.school.projectservice.model.CampaignStatus.ACTIVE;
import static faang.school.projectservice.model.CampaignStatus.CANCELED;
import static faang.school.projectservice.model.CampaignStatus.COMPLETED;

@RequiredArgsConstructor
@Service
public class CampaignService {
    private final ProjectService projectService;
    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final CampaignValidationService campaignValidationService;

    @Transactional
    public Campaign createCampaign(Campaign campaign, Long projectId, Long userId) {
        Project project = projectService.getProjectById(projectId);
        campaignValidationService.validateUserCanCreateCampaign(campaign, project, userId);

        campaign.setProject(project);
        campaign.setCreatedBy(userId);
        campaign.setStatus(ACTIVE);
        return campaignRepository.save(campaign);
    }

    public Campaign getCampaignById(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    String exceptionMessage = String.format("Campaign with id %d not found", campaignId);
                    return new NoSuchElementException(exceptionMessage);
                });
    }

    @Transactional
    public Campaign updateCampaign(Long campaignId, Long userId, CampaignDto campaignDto) {
        Campaign campaign = getCampaignById(campaignId);
        campaignValidationService.validateUserCanUpdateCampaign(campaign, userId, campaignDto);

        campaign = campaignMapper.updateCampaignFromDto(campaignDto, campaign);

        if (campaign.getStatus() != CANCELED) {
            if (campaign.getGoal() != null && campaign.getAmountRaised() != null && campaign.getGoal().compareTo(campaign.getAmountRaised()) <= 0) {
                campaign.setStatus(COMPLETED);
            } else {
                campaign.setStatus(ACTIVE);
            }
        }

        campaign.setUpdatedBy(userId);

        return campaignRepository.save(campaign);
    }

    @Transactional
    public Campaign deleteCampaign(Long campaignId, Long userId) {
        Campaign campaign = getCampaignById(campaignId);
        campaignValidationService.validateUserCanDeleteCampaign(campaign, userId);

        campaign.setStatus(CANCELED);
        campaign.setUpdatedBy(userId);

        return campaignRepository.save(campaign);
    }

    public List<Campaign> getCampaignsByProjectIdAndFilter(Long projectId, CampaignFilterDto filter) {
        return campaignRepository.findAllByFiltersAndProjectId(projectId,
                filter.getIdCreatedBy(),
                filter.getTitlePattern(),
                filter.getMinGoal(),
                filter.getMaxGoal(),
                filter.getCreatedDateFrom(),
                filter.getCreatedDateTo(),
                filter.getUpdatedDateFrom(),
                filter.getUpdatedDateTo(),
                filter.getStatus());
    }

}
