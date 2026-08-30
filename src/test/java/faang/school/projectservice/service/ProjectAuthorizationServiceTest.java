package faang.school.projectservice.service;

import faang.school.projectservice.exception.AccessDeniedException;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectVisibility;
import faang.school.projectservice.model.TeamMember;
import faang.school.projectservice.repository.TeamMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAuthorizationServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private ProjectAuthorizationService authorizationService;

    @Test
    void allowsPublicProjectRead() {
        Project project = project(ProjectVisibility.PUBLIC, 1L);

        assertDoesNotThrow(() -> authorizationService.requireViewAccess(project, 99L));
    }

    @Test
    void allowsPrivateProjectReadForTeamMember() {
        Project project = project(ProjectVisibility.PRIVATE, 1L);
        when(teamMemberRepository.findByUserIdAndProjectId(7L, 42L))
                .thenReturn(TeamMember.builder().userId(7L).build());

        assertDoesNotThrow(() -> authorizationService.requireViewAccess(project, 7L));
    }

    @Test
    void deniesPrivateProjectReadForUnrelatedUser() {
        Project project = project(ProjectVisibility.PRIVATE, 1L);

        assertThrows(AccessDeniedException.class,
                () -> authorizationService.requireViewAccess(project, 99L));
    }

    @Test
    void permitsOnlyOwnerToModifyProject() {
        Project project = project(ProjectVisibility.PUBLIC, 1L);

        assertDoesNotThrow(() -> authorizationService.requireOwner(project, 1L));
        assertThrows(AccessDeniedException.class,
                () -> authorizationService.requireOwner(project, 2L));
    }

    private Project project(ProjectVisibility visibility, long ownerId) {
        return Project.builder().id(42L).ownerId(ownerId).visibility(visibility).build();
    }
}
