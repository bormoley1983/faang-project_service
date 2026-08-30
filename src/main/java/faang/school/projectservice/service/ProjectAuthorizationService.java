package faang.school.projectservice.service;

import faang.school.projectservice.exception.AccessDeniedException;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectVisibility;
import faang.school.projectservice.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectAuthorizationService {

    private final TeamMemberRepository teamMemberRepository;

    public void requireViewAccess(Project project, long userId) {
        if (project.getVisibility() == ProjectVisibility.PUBLIC
                || Objects.equals(project.getOwnerId(), userId)
                || teamMemberRepository.findByUserIdAndProjectId(userId, project.getId()) != null) {
            return;
        }
        throw new AccessDeniedException("User is not allowed to view project " + project.getId());
    }

    public void requireOwner(Project project, long userId) {
        if (!Objects.equals(project.getOwnerId(), userId)) {
            throw new AccessDeniedException("Only the project owner may modify project " + project.getId());
        }
    }
}
