package faang.school.projectservice.service;

import faang.school.projectservice.dto.campaign.CampaignDto;
import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.TeamMember;
import faang.school.projectservice.model.TeamRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static faang.school.projectservice.model.CampaignStatus.ACTIVE;
import static faang.school.projectservice.model.CampaignStatus.CANCELED;
import static faang.school.projectservice.model.CampaignStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CampaignValidationServiceTest {

    @Mock
    private TeamMemberService teamMemberService;

    @InjectMocks
    private CampaignValidationService campaignValidationService;

    private Campaign campaign;
    private Project project;
    private TeamMember teamMember;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setOwnerId(1L);

        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTitle("Title");
        campaign.setCreatedBy(1L);
        campaign.setProject(project);

        teamMember = new TeamMember();
        teamMember.setUserId(1L);
        teamMember.setRoles(List.of(TeamRole.MANAGER));
    }

    @Test
    void validateUserCanCreateCampaign_ShouldPassForProjectOwner() {
        teamMember.setRoles(List.of());
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.of(teamMember));

        assertDoesNotThrow(() -> campaignValidationService.validateUserCanCreateCampaign(campaign, project, 1L));
    }

    @Test
    void validateUserCanCreateCampaign_ShouldPassForManager() {
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.of(teamMember));

        assertDoesNotThrow(() -> campaignValidationService.validateUserCanCreateCampaign(campaign, project, 1L));
    }

    @Test
    void validateUserCanCreateCampaign_ShouldThrowExceptionForEmptyTitle() {
        campaign.setTitle("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignValidationService.validateUserCanCreateCampaign(campaign, project, 1L));

        assertEquals("Title of the campaign cannot be empty", exception.getMessage());
    }

    @Test
    void validateUserCanCreateCampaign_ShouldThrowExceptionForNonMember() {
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> campaignValidationService.validateUserCanCreateCampaign(campaign, project, teamMember.getUserId()));
        assertEquals(String.format("User with id %d is not a team member of project with id %d", teamMember.getUserId(), project.getId()), exception.getMessage());
    }

    @Test
    void validateUserCanCreateCampaign_ShouldThrowExceptionForNonManagerAndNotOwner() {
        teamMember.setRoles(List.of(TeamRole.DEVELOPER));
        project.setOwnerId(2L);
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.of(teamMember));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> campaignValidationService.validateUserCanCreateCampaign(campaign, project, 1L));

        assertEquals("User with id 1 cannot create a fundraising! Only the project owner or a manager can create a campaign", exception.getMessage());
    }

    @Test
    void validateUserCanUpdateCampaign_ShouldPassForValidUpdate() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setIdCreatedBy(1L);
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.of(teamMember));

        assertDoesNotThrow(() -> campaignValidationService.validateUserCanUpdateCampaign(campaign, 1L, campaignDto));
    }

    @Test
    void validateUserCanUpdateCampaign_ShouldThrowExceptionForCanceledCampaign() {
        campaign.setStatus(CANCELED);
        CampaignDto campaignDto = new CampaignDto();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignValidationService.validateUserCanUpdateCampaign(campaign, 1L, campaignDto));

        assertEquals(String.format("Campaign with id %d is already CANCELED and cannot be updated", campaign.getId()), exception.getMessage());
    }

    @Test
    void validateUserCanUpdateCampaign_ShouldThrowExceptionForInvalidCreatorChange() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setIdCreatedBy(2L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignValidationService.validateUserCanUpdateCampaign(campaign, 1L, campaignDto));

        assertEquals("Creator of the campaign cannot be changed", exception.getMessage());
    }

    @Test
    void validateUserCanUpdateCampaign_ShouldThrowExceptionForInvalidStatusChange() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setStatus(ACTIVE);
        campaign.setStatus(COMPLETED);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignValidationService.validateUserCanUpdateCampaign(campaign, 1L, campaignDto));

        assertEquals(String.format("Campaign with status %s cannot be changed to ACTIVE", COMPLETED), exception.getMessage());
    }

    @Test
    void validateUserCanUpdateCampaign_ShouldThrowExceptionForProjectChange() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setProjectId(2L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignValidationService.validateUserCanUpdateCampaign(campaign, 1L, campaignDto));

        assertEquals("Project of the campaign cannot be changed", exception.getMessage());
    }

    @Test
    void validateUserCanUpdateCampaign_ShouldThrowExceptionForNotMember() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setIdCreatedBy(teamMember.getUserId());
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> campaignValidationService.validateUserCanUpdateCampaign(campaign, teamMember.getUserId(), campaignDto));

        assertEquals(String.format("User with id %d is not a team member of project with id %d", teamMember.getUserId(), project.getId()), exception.getMessage());
    }

    @Test
    void validateUserCanDeleteCampaign_ShouldPassForValidDeletion() {
        campaign.setStatus(ACTIVE);
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.of(teamMember));

        assertDoesNotThrow(() -> campaignValidationService.validateUserCanDeleteCampaign(campaign, teamMember.getUserId()));
    }

    @Test
    void validateUserCanDeleteCampaign_ShouldThrowExceptionForAlreadyCanceled() {
        campaign.setStatus(CANCELED);
        when(teamMemberService.getTeamMemberByUserIdAndProjectId(anyLong(), anyLong())).thenReturn(Optional.of(teamMember));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> campaignValidationService.validateUserCanDeleteCampaign(campaign, teamMember.getUserId()));

        assertEquals("Campaign is already canceled", exception.getMessage());
    }
}