package faang.school.projectservice.controller;

import faang.school.projectservice.dto.campaign.CampaignDto;
import faang.school.projectservice.dto.campaign.CampaignFilterDto;
import faang.school.projectservice.mapper.campaign.CampaignMapper;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.service.CampaignService;
import jakarta.validation.constraints.NotNull;
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

    @PostMapping("/project/{projectId}/user/{userId}")
    public ResponseEntity<CampaignDto> createCampaign(@PathVariable @Positive @NotNull Long projectId,
                                                      @PathVariable @Positive @NotNull Long userId,
                                                      @RequestBody CampaignDto campaignDto) {
        Campaign campaignToCreate = campaignMapper.toEntity(campaignDto);
        Campaign createdCampaign = campaignService.createCampaign(campaignToCreate, projectId, userId);
        CampaignDto createdCampaignDto = campaignMapper.toDto(createdCampaign);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCampaignDto);
    }

    @PatchMapping("/{campaignId}/user/{userId}")
    public ResponseEntity<CampaignDto> updateCampaign(@PathVariable @Positive @NotNull Long campaignId,
                                                      @PathVariable @Positive @NotNull Long userId,
                                                      @RequestBody CampaignDto campaignDto) {

        Campaign updatedCampaign = campaignService.updateCampaign(campaignId, userId, campaignDto);
        CampaignDto updatedCampaignDto = campaignMapper.toDto(updatedCampaign);

        return ResponseEntity.ok().body(updatedCampaignDto);
    }

    @DeleteMapping("/{campaignId}/user/{userId}")
    public ResponseEntity<CampaignDto> deleteCampaign(@PathVariable @Positive @NotNull Long campaignId,
                                                      @PathVariable @Positive @NotNull Long userId) {
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
                                                                   @RequestBody CampaignFilterDto campaignFilterDto) {

        List<Campaign> campaignList = campaignService.getCampaignsByProjectIdAndFilter(projectId, campaignFilterDto);
        List<CampaignDto> campaignDtoList = campaignMapper.toDtoList(campaignList);
        return ResponseEntity.ok().body(campaignDtoList);
    }
}
