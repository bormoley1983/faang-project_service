package faang.school.projectservice.controller;

import faang.school.projectservice.dto.campaign.CampaignDto;
import faang.school.projectservice.dto.campaign.CampaignFilterDto;
import faang.school.projectservice.config.context.user.UserContext;
import faang.school.projectservice.mapper.campaign.CampaignMapper;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.service.CampaignService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/campaign")
@RestController
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignMapper campaignMapper;
    private final UserContext userContext;

    @PostMapping("/project/{projectId}")
    public ResponseEntity<CampaignDto> createCampaign(@PathVariable @Positive @NotNull Long projectId,
                                                      @Valid @RequestBody CampaignDto campaignDto) {
        long userId = userContext.getUserId();
        Campaign campaignToCreate = campaignMapper.toEntity(campaignDto);
        Campaign createdCampaign = campaignService.createCampaign(campaignToCreate, projectId, userId);
        CampaignDto createdCampaignDto = campaignMapper.toDto(createdCampaign);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCampaignDto);
    }

    @PatchMapping("/{campaignId}")
    public ResponseEntity<CampaignDto> updateCampaign(@PathVariable @Positive @NotNull Long campaignId,
                                                      @Valid @RequestBody CampaignDto campaignDto) {
        long userId = userContext.getUserId();
        Campaign updatedCampaign = campaignService.updateCampaign(campaignId, userId, campaignDto);
        CampaignDto updatedCampaignDto = campaignMapper.toDto(updatedCampaign);

        return ResponseEntity.ok().body(updatedCampaignDto);
    }

    @DeleteMapping("/{campaignId}")
    public ResponseEntity<CampaignDto> deleteCampaign(@PathVariable @Positive @NotNull Long campaignId) {
        long userId = userContext.getUserId();
        Campaign deletedCampaign = campaignService.deleteCampaign(campaignId, userId);
        CampaignDto deletedCampaignDto = campaignMapper.toDto(deletedCampaign);
        return ResponseEntity.ok().body(deletedCampaignDto);
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<CampaignDto> getCampaign(@PathVariable @Positive @NotNull Long campaignId) {
        Campaign campaign = campaignService.getCampaignById(campaignId);
        CampaignDto campaignDto = campaignMapper.toDto(campaign);
        return ResponseEntity.ok().body(campaignDto);
    }

    @PostMapping("/get-by-project/{projectId}")
    public ResponseEntity<List<CampaignDto>> getCampaignsByProject(@PathVariable @Positive @NotNull Long projectId,
                                                                   @RequestBody(required = false) CampaignFilterDto campaignFilterDto) {

        List<Campaign> campaignList = campaignService.getCampaignsByProjectIdAndFilter(projectId, campaignFilterDto);
        List<CampaignDto> campaignDtoList = campaignMapper.toDtoList(campaignList);
        return ResponseEntity.ok().body(campaignDtoList);
    }
}
