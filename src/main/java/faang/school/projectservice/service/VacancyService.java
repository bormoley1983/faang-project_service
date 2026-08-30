package faang.school.projectservice.service;

import faang.school.projectservice.exseption.ProjectNotFoundException;
import faang.school.projectservice.exseption.VacancyNotFoundException;
import faang.school.projectservice.model.Candidate;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.TeamRole;
import faang.school.projectservice.model.Vacancy;
import faang.school.projectservice.repository.CandidateRepository;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class VacancyService {

    private static final Logger log = LoggerFactory.getLogger(VacancyService.class);
    private final VacancyRepository vacancyRepository;
    private final VacancyValidatorService validatorService ;
    private final CandidateRepository candidateRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public Vacancy createVacancy(Vacancy vacancy, Long currentUserIds, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        validatorService.validateUserCanManageProject(currentUserIds, project);
        vacancy.setProject(project);
        vacancyRepository.save(vacancy);
        return vacancy;
    }

    @Transactional
    public Vacancy updateVacancy(Vacancy vacancy, Long vacancyId, Long currentUserIds) {
        Vacancy vacancyToUpdate = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy Not Found"));

        validatorService.validateUserCanManageProject(currentUserIds, vacancyToUpdate.getProject());
        validatorService.validateVacancyCanBeClosed(vacancyToUpdate, vacancy.getStatus());

        if (vacancy.getPosition() != null) {
            vacancyToUpdate.setPosition(vacancy.getPosition());
        }
        if (vacancy.getCount() != null) {
            vacancyToUpdate.setCount(vacancy.getCount());
        }
        if (vacancy.getStatus() != null) {
            vacancyToUpdate.setStatus(vacancy.getStatus());
        }

        vacancyRepository.save(vacancyToUpdate);
        return vacancyToUpdate;
    }

    @Transactional
    public void addCandidatesToVacancy(List<Long> candidateIds, Long projectId, Long vacancyId, Long currentUserIds) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy Not Found"));

        if (!Objects.equals(vacancy.getProject().getId(), projectId)) {
            throw new IllegalArgumentException("Vacancy does not belong to project " + projectId);
        }
        validatorService.validateUserCanManageProject(currentUserIds, vacancy.getProject());
        List<Candidate> candidates = candidateRepository.findAllById(candidateIds);
        if (candidates.size() != candidateIds.stream().distinct().count()) {
            throw new IllegalArgumentException("One or more candidates do not exist");
        }
        if (!candidateIds.isEmpty()) {
            validatorService.ensureCandidatesAreNotProjectMembers(candidates, projectId);
        }

        Set<Long> existingCandidateIds = vacancy.getCandidates().stream()
                .map(Candidate::getId)
                .collect(Collectors.toSet());
        List<Long> newCandidateIds = candidateIds.stream()
                .filter(candidateId -> !existingCandidateIds.contains(candidateId))
                .toList();
        if (!newCandidateIds.isEmpty()) {
            List<Candidate> newCandidates = candidates.stream()
                    .filter(candidate -> newCandidateIds.contains(candidate.getId()))
                    .toList();
            vacancy.getCandidates().addAll(newCandidates);
        }

        vacancyRepository.save(vacancy);
    }

    @Transactional
    public void removeVacancy(Long vacancyId, Long currentUserIds) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy Not Found"));
        validatorService.validateUserCanManageProject(currentUserIds, vacancy.getProject());
        vacancyRepository.delete(vacancy);
        log.info("Vacancy with ID {} successfully deleted by user {}", vacancyId, currentUserIds);
    }

    @Transactional
    public Page<Vacancy> filterVacancies(TeamRole position, String name, Pageable pageable) {
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        return vacancyRepository.search(position, normalizedName, pageable);
    }

    @Transactional
    public Vacancy getVacancyById(Long vacancyId) {
        return vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy Not Found"));
    }
}
