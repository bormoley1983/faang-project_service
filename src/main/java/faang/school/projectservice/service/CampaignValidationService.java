package faang.school.projectservice.service;

import faang.school.projectservice.dto.campaign.CampaignDto;
import faang.school.projectservice.exception.AccessDeniedException;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.TeamMember;
import faang.school.projectservice.model.TeamRole;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

import static faang.school.projectservice.model.CampaignStatus.ACTIVE;
import static faang.school.projectservice.model.CampaignStatus.CANCELED;

@RequiredArgsConstructor
@Service
public class CampaignValidationService {

    private final TeamMemberService teamMemberService;

    public void validateUserCanCreateCampaign(Campaign campaign, Project project, Long userId) {
        if (StringUtils.isBlank(campaign.getTitle())) {
            throw new IllegalArgumentException("Title of the campaign cannot be empty");
        }

        requireOwnerOrManager(project, userId);
    }

    public void validateUserCanUpdateCampaign(Campaign campaign, Long userId, CampaignDto campaignDto) {

        if (campaign.getStatus() == CANCELED ) {
            String exceptionMessage = String.format("Campaign with id %d is already CANCELED and cannot be updated", campaign.getId());
            throw new IllegalArgumentException(exceptionMessage);
        }

        if (campaignDto.getIdCreatedBy() != null && !Objects.equals(campaignDto.getIdCreatedBy(), campaign.getCreatedBy())) {
            throw new IllegalArgumentException("Creator of the campaign cannot be changed");
        }

        if (campaign.getStatus() != ACTIVE && campaignDto.getStatus() == ACTIVE) {
            String exceptionMessage = String.format("Campaign with status %s cannot be changed to ACTIVE", campaign.getStatus());
            throw new IllegalArgumentException(exceptionMessage);
        }

        if (campaignDto.getProjectId() != null && !Objects.equals(campaignDto.getProjectId(), campaign.getProject().getId())) {
            throw new IllegalArgumentException("Project of the campaign cannot be changed");
        }

        requireOwnerOrManager(campaign.getProject(), userId);
    }

    public void validateUserCanDeleteCampaign(Campaign campaign, Long userId) {
        requireOwnerOrManager(campaign.getProject(), userId);

        if (campaign.getStatus() == CANCELED) {
            throw new UnsupportedOperationException("Campaign is already canceled");
        }
    }

    private TeamMember getTeamMemberByUserIdAndProjectId(Long userId, Long projectId) {
        return teamMemberService.getTeamMemberByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> {
                    String exceptionMessage = String.format("User with id %d is not a team member of project with id %d", userId, projectId);
                    return new NoSuchElementException(exceptionMessage);
                });
    }

    private void requireOwnerOrManager(Project project, Long userId) {
        if (Objects.equals(project.getOwnerId(), userId)) {
            return;
        }

        TeamMember member = getTeamMemberByUserIdAndProjectId(userId, project.getId());
        if (member.getRoles() == null || !member.getRoles().contains(TeamRole.MANAGER)) {
            throw new AccessDeniedException(
                    "Only the project owner or a project manager may modify campaigns");
        }
    }
}
