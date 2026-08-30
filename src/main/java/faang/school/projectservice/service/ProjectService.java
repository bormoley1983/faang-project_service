package faang.school.projectservice.service;

import faang.school.projectservice.config.ProjectProperties;
import faang.school.projectservice.event.ProjectCalendarProvisioningRequested;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.model.Resource;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.service.s3.S3Service;
import faang.school.projectservice.service.s3.StorageTransactionCoordinator;
import faang.school.projectservice.validator.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectAuthorizationService projectAuthorizationService;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;
    private final StorageTransactionCoordinator storageTransactionCoordinator;
    private final ProjectProperties projectProperties;
    private final FileValidator fileValidator;
    private final ImageResizer imageResizer;

    @Transactional
    public Project createProject(Project project, Long ownerId) {
        initializeProjectDetails(project, ownerId);
        Project savedProject = projectRepository.save(project);
        requestCalendarProvisioning(savedProject);
        return savedProject;
    }

    @Transactional
    public Project createSubProject(Project subProject, Long ownerId) {
        Project parentProject = findProjectById(subProject.getParentProject().getId());
        projectAuthorizationService.requireOwner(parentProject, ownerId);
        subProject.setParentProject(parentProject);
        initializeProjectDetails(subProject, ownerId);
        Project savedSubProject = projectRepository.save(subProject);
        requestCalendarProvisioning(savedSubProject);
        return savedSubProject;
    }

    @Transactional
    public Project updateProject(Project project, long userId) {
        Project existingProject = findProjectById(project.getId());
        projectAuthorizationService.requireOwner(existingProject, userId);
        updateProjectDetails(existingProject, project);
        return projectRepository.save(existingProject);
    }

    @Transactional
    public Project updateSubProject(Project subProject, long userId) {
        Project existingSubProject = findProjectById(subProject.getId());
        projectAuthorizationService.requireOwner(existingSubProject, userId);
        updateProjectDetails(existingSubProject, subProject);
        return projectRepository.save(existingSubProject);
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjects(String name, ProjectStatus status, Long userId, Pageable pageable) {
        return projectRepository.findVisibleProjects(normalizeFilter(name), status, userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Project> getSubProjects(Long parentProjectId, String name, ProjectStatus status,
                                        Long userId, Pageable pageable) {
        return projectRepository.findVisibleSubProjects(
                parentProjectId, normalizeFilter(name), status, userId, pageable);
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
        projectAuthorizationService.requireViewAccess(project, userId);
        return project;
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByIds(List<Long> projectIds, long userId) {
        if (projectIds == null || projectIds.isEmpty() || projectIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Project IDs must not be null or empty");
        }

        Set<Long> requestedIds = new LinkedHashSet<>(projectIds);
        List<Project> projects = projectRepository.findAllById(requestedIds);
        Set<Long> foundIds = projects.stream().map(Project::getId).collect(Collectors.toSet());
        Set<Long> missingIds = requestedIds.stream()
                .filter(projectId -> !foundIds.contains(projectId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!missingIds.isEmpty()) {
            throw new IllegalArgumentException("Projects not found: " + missingIds);
        }

        projects.forEach(project -> {
            projectAuthorizationService.requireViewAccess(project, userId);
        });

        return projects;
    }

    @Transactional(readOnly = true)
    public List<Long> getUserIdsByProjectIds(List<Long> projectIds) {
        return projectRepository.getUserIdsByProjectIds(projectIds);
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

    private void requestCalendarProvisioning(Project project) {
        eventPublisher.publishEvent(new ProjectCalendarProvisioningRequested(project.getId(), project.getName()));
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public void uploadProjectCover(Long projectId, MultipartFile file, long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        projectAuthorizationService.requireOwner(project, userId);

        if (project.getCoverImageId() != null) {
            throw new IllegalStateException("Project cover already exists. " +
                    "Delete the existing cover before uploading a new one.");
        }

        fileValidator.validateFile(file);

        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] resizedImageBytes = imageResizer.resizeImage(
                imageBytes,
                projectProperties.getTargetWidth(),
                projectProperties.getTargetHeight());

        MultipartFile resizedFile = new ResizedMultipartFile(file, resizedImageBytes);
        Resource resource = s3Service.uploadFile(resizedFile, "covers");
        storageTransactionCoordinator.deleteOnRollback(resource.getKey());

        project.setCoverImageId(resource.getKey());
        projectRepository.save(project);
    }

    @Transactional
    public void deleteCover(Long projectId, long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        projectAuthorizationService.requireOwner(project, userId);

        if (project.getCoverImageId() != null) {
            String coverKey = project.getCoverImageId();
            project.setCoverImageId(null);
            projectRepository.save(project);
            storageTransactionCoordinator.deleteAfterCommit(coverKey);
        } else {
            throw new IllegalStateException("Project cover does not exist. Nothing to delete.");
        }
    }
}
