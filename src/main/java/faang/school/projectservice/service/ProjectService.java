package faang.school.projectservice.service;

import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.model.ProjectVisibility;
import faang.school.projectservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public Project createProject(Project project, Long ownerId) {
        validateProjectNameUniqueness(ownerId, project.getName());
        project.setOwnerId(ownerId);
        project.setStatus(ProjectStatus.CREATED);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    @Transactional
    public Project createSubProject(Project subProject, Long ownerId) {
        Project parentProject = findProjectById(subProject.getParentProject().getId());
        subProject.setParentProject(parentProject);
        subProject.setOwnerId(ownerId);
        subProject.setStatus(ProjectStatus.CREATED);
        subProject.setCreatedAt(LocalDateTime.now());
        subProject.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(subProject);
    }

    @Transactional
    public Project updateProject(Project project) {
        Project existingProject = findProjectById(project.getId());
        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(existingProject);
    }

    @Transactional
    public Project updateSubProject(Project subProject) {
        Project existingSubProject = findProjectById(subProject.getId());
        existingSubProject.setName(subProject.getName());
        existingSubProject.setDescription(subProject.getDescription());
        existingSubProject.setUpdatedAt(LocalDateTime.now());
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
}