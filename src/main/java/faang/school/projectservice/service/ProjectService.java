package faang.school.projectservice.service;

import com.google.api.services.calendar.model.Calendar;
import faang.school.projectservice.model.Meet;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.model.ProjectVisibility;
import faang.school.projectservice.model.Schedule;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.service.google.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final GoogleCalendarService googleCalendarService;
    private final ProjectScheduleService projectScheduleService;
    private final ProjectMeetService projectMeetService;

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Transactional
    public Project createProject(Project project, Long ownerId) {
        initializeProjectDetails(project, ownerId);
        createProjectCalendar(project);
        return projectRepository.save(project);
    }

    @Transactional
    public Project createSubProject(Project subProject, Long ownerId) {
        Project parentProject = findProjectById(subProject.getParentProject().getId());
        subProject.setParentProject(parentProject);
        initializeProjectDetails(subProject, ownerId);
        createProjectCalendar(subProject);
        return projectRepository.save(subProject);
    }

    @Transactional
    public Project updateProject(Project project) {
        Project existingProject = findProjectById(project.getId());
        updateProjectDetails(existingProject, project);
        updateProjectCalendar(existingProject, project);
        return projectRepository.save(existingProject);
    }

    @Transactional
    public Project updateSubProject(Project subProject) {
        Project existingSubProject = findProjectById(subProject.getId());
        updateProjectDetails(existingSubProject, subProject);
        updateProjectCalendar(existingSubProject, subProject);
        return projectRepository.save(existingSubProject);
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjects(String name, ProjectStatus status, Long userId, Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Project> getSubProjects(Long parentProjectId, String name, ProjectStatus status, Pageable pageable) {
        return projectRepository.findByParentProjectId(parentProjectId, pageable);
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long projectId) {
        return findProjectById(projectId);
    }

    private void validateProjectNameUniqueness(Long ownerId, String name) {
        if (projectRepository.existsByOwnerIdAndName(ownerId, name)) {
            throw new IllegalArgumentException("Project with the same name already exists");
        }
    }

    @Transactional(readOnly = true)
    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    @Transactional(readOnly = true)
    public Project getProjectById(long projectId, long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!isProjectVisible(project, userId)) {
            throw new IllegalArgumentException("You don't have access to this project");
        }

        return project;
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByIds(List<Long> projectIds, long userId) {
        List<Project> projects = projectRepository.findAllById(projectIds);

        projects.forEach(project -> {
            if (!isProjectVisible(project, userId)) {
                throw new IllegalArgumentException("You don't have access to project by id " + project.getId());
            }
        });

        return projects;
    }

    @Transactional(readOnly = true)
    public List<Long> getUserIdsByProjectIds(List<Long> projectIds) {
        return projectRepository.getUserIdsByProjectIds(projectIds);
    }

    private boolean isProjectVisible(Project project, Long userId) {
        return project.getVisibility() == ProjectVisibility.PUBLIC || project.getOwnerId().equals(userId);
    }

    private void initializeProjectDetails(Project project, Long ownerId) {
        validateProjectNameUniqueness(ownerId, project.getName());
        project.setOwnerId(ownerId);
        project.setStatus(ProjectStatus.CREATED);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
    }

    private void updateProjectDetails(Project existingProject, Project project) {
        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setUpdatedAt(LocalDateTime.now());
    }

    private void createProjectCalendar(Project project) {
        Calendar calendar = new Calendar();
        calendar.setSummary(project.getName());
        calendar.setDescription("Calendar for project: " + project.getName());

        Calendar createdCalendar = googleCalendarService.createCalendar(calendar);
        project.setGoogleCalendarId(createdCalendar.getId());
    }

    private void updateProjectCalendar(Project existingProject, Project project) {
        String googleCalendarId = project.getGoogleCalendarId();

        Schedule existingSchedule = existingProject.getSchedule();
        Schedule schedule = project.getSchedule();

        if (!Objects.equals(existingSchedule, schedule)) {
            projectScheduleService.getScheduleEvent(googleCalendarId, schedule.getGoogleEventId());
            // update existing schedule event
        }

        List<Meet> existingMeets = existingProject.getMeets() != null ?
                existingProject.getMeets() : Collections.emptyList();
        List<Meet> meets = project.getMeets() != null ?
                project.getMeets() : Collections.emptyList();

        Map<Long, Meet> existingMeetsMap = existingMeets.stream()
                .collect(Collectors.toMap(Meet::getId, meet -> meet));

        meets.forEach(meet -> {
            if (existingMeetsMap.containsKey(meet.getId())) {
                projectMeetService.getMeetEvent(googleCalendarId, meet.getGoogleEventId());
                // Update existing meet event
            } else {
                projectMeetService.createMeetEvent(googleCalendarId, meet);
                meet.setGoogleEventId(meet.getGoogleEventId());
            }
        });

        existingMeets.stream()
                .filter(meet -> !meets.contains(meet))
                .forEach(meet -> projectMeetService.deleteMeetEvent(googleCalendarId, meet.getGoogleEventId()));

        project.setMeets(meets);
    }
}