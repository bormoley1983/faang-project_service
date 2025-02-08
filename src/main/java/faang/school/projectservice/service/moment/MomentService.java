package faang.school.projectservice.service.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.exception.ResourceNotFoundException;
import faang.school.projectservice.filter.moment.MomentFilter;
import faang.school.projectservice.model.Moment;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.repository.MomentRepository;
import faang.school.projectservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class MomentService {

    private final MomentRepository momentRepository;
    private final ProjectService projectService;
    private final List<MomentFilter> momentFilters;

    @Transactional
    public Moment createMoment(long userId, long projectId, Moment momentRequest, List<Long> projectIds) {
        projectService.getProjectById(projectId, userId);

        List<Long> newProjectIds = new ArrayList<>(projectIds);
        newProjectIds.add(projectId);

        List<Project> projects = projectService.getProjectsByIds(newProjectIds, userId);
        List<Long> userIds = projectService.getUserIdsByProjectIds(newProjectIds);

        momentRequest.setProjects(projects);
        momentRequest.setUserIds(userIds);
        momentRequest.setCreatedBy(userId);
        momentRequest.setUpdatedBy(userId);

        return momentRepository.save(momentRequest);
    }

    @Transactional
    public Moment updateMoment(long userId, long momentId, Moment momentRequest, List<Long> newProjectIds) {

        Moment existingMoment = momentRepository.findById(momentId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found moment by id: " + momentId));

        List<Project> projects = projectService.getProjectsByIds(newProjectIds, userId);
        if (projects.isEmpty()) {
            throw new IllegalArgumentException("Moment must have at least one project");
        }

        List<Long> userIds = projectService.getUserIdsByProjectIds(newProjectIds);

        existingMoment.setName(momentRequest.getName());
        existingMoment.setDescription(momentRequest.getDescription());
        existingMoment.setDate(momentRequest.getDate());
        existingMoment.setProjects(projects);
        existingMoment.setUserIds(userIds);
        existingMoment.setUpdatedBy(userId);

        return momentRepository.save(existingMoment);
    }

    @Transactional(readOnly = true)
    public List<Moment> getAllMomentsByProjectId(long projectId, MomentFilterDto filters) {
        Stream<Moment> moments = momentRepository.getAllByProjectId(projectId);
        return applyFilters(moments, filters).toList();
    }

    @Transactional(readOnly = true)
    public Moment getMomentById(long momentId) {
        return momentRepository.findById(momentId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found moment by id: " + momentId));
    }

    private Stream<Moment> applyFilters(Stream<Moment> moments, MomentFilterDto filters) {
        List<MomentFilter> applicableFilters = momentFilters.stream()
                .filter(filter -> filter.isApplicable(filters))
                .toList();

        return moments.filter(moment ->
                applicableFilters.stream()
                        .allMatch(momentFilter ->
                                momentFilter.apply(moment, filters)));
    }
}
