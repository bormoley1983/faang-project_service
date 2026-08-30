package faang.school.projectservice.service;


import faang.school.projectservice.exception.AccessDeniedException;
import faang.school.projectservice.model.TeamMember;
import faang.school.projectservice.model.TeamRole;
import faang.school.projectservice.model.Candidate;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.Vacancy;
import faang.school.projectservice.model.VacancyStatus;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VacancyValidatorService {
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;

    public void validateUserCanManageProject(Long userId, Project project) {
        if (project.getOwnerId().equals(userId)) {
            return;
        }
        TeamMember curator = teamMemberRepository.findByUserIdAndProjectId(userId, project.getId());

        if (curator == null || (!curator.getRoles().contains(TeamRole.OWNER) &&
                !curator.getRoles().contains(TeamRole.MANAGER))) {
            throw new AccessDeniedException(
                    "User must be the project owner or a project manager");
        }
    }

    public void ensureCandidatesAreNotProjectMembers(List<Candidate> candidates, Long projectId) {
        List<Long> projectMembers = projectRepository.findAllTeamMemberUserIdsByProjectId(projectId);
        candidates.forEach(candidate -> {
            if (projectMembers.contains(candidate.getUserId())) {
                throw new IllegalArgumentException(
                        "Candidate user " + candidate.getUserId() + " is already in project " + projectId);
            }
        });
    }

    public void validateVacancyCanBeClosed(Vacancy vacancy, VacancyStatus status) {
        if (status == VacancyStatus.CLOSED) {
            if (vacancy.getCandidates().isEmpty()) {
                throw new IllegalArgumentException("Vacancy has no candidates");
            }
            if (vacancy.getCandidates().size() < vacancy.getCount()) {
                throw new IllegalArgumentException("Vacancy has not enough candidates");
            }
        }
    }
}
