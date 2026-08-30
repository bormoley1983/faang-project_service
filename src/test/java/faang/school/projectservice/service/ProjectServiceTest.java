package faang.school.projectservice.service;

import faang.school.projectservice.config.ProjectProperties;
import faang.school.projectservice.event.ProjectCalendarProvisioningRequested;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.model.Schedule;
import faang.school.projectservice.model.Resource;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.service.s3.S3Service;
import faang.school.projectservice.service.s3.StorageTransactionCoordinator;
import faang.school.projectservice.validator.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectAuthorizationService projectAuthorizationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private S3Service s3Service;

    @Mock
    private StorageTransactionCoordinator storageTransactionCoordinator;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private ImageResizer imageResizer;

    @Mock
    private MultipartFile file;

    @Spy
    private ProjectProperties projectProperties;

    @InjectMocks
    private ProjectService projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .ownerId(1L)
                .status(ProjectStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createProject_ShouldSaveAndReturnProject() {
        when(projectRepository.existsByOwnerIdAndName(project.getOwnerId(), project.getName())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        Project result = projectService.createProject(project, project.getOwnerId());

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectRepository, times(1)).save(project);
        verify(eventPublisher).publishEvent(
                new ProjectCalendarProvisioningRequested(project.getId(), project.getName()));
    }

    @Test
    void createProject_DoesNotCallCalendarBeforeDatabaseCommit() {
        when(projectRepository.existsByOwnerIdAndName(project.getOwnerId(), project.getName())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.createProject(project, project.getOwnerId());

        assertNotNull(result);
        assertNull(result.getGoogleCalendarId());
        verify(projectRepository).save(project);
        verify(eventPublisher).publishEvent(any(ProjectCalendarProvisioningRequested.class));
    }

    @Test
    void createProject_ShouldThrowExceptionIfProjectExists() {
        when(projectRepository.existsByOwnerIdAndName(project.getOwnerId(), project.getName())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.createProject(project, project.getOwnerId()));

        assertEquals("Project with the same name already exists", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateProject_ShouldUpdateAndReturnProject() {
        Schedule persistedSchedule = new Schedule();
        persistedSchedule.setGoogleEventId("persisted-schedule-event");
        project.setSchedule(persistedSchedule);
        project.setGoogleCalendarId("persisted-calendar");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project metadataUpdate = Project.builder()
                .id(project.getId())
                .name("Renamed Project")
                .description("Updated description")
                .build();
        Project updatedProject = projectService.updateProject(metadataUpdate, 1L);

        assertNotNull(updatedProject);
        assertEquals("Renamed Project", updatedProject.getName());
        assertEquals("persisted-calendar", updatedProject.getGoogleCalendarId());
        assertEquals(persistedSchedule, updatedProject.getSchedule());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void updateProject_ShouldThrowExceptionIfNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.updateProject(project, 1L));

        assertEquals("Project not found", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getProjectsPassesFiltersAndViewerToVisibilityQuery() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Project> expected = new PageImpl<>(java.util.List.of(project), pageable, 1);
        when(projectRepository.findVisibleProjects(
                "Roadmap", ProjectStatus.CREATED, 7L, pageable)).thenReturn(expected);

        Page<Project> result = projectService.getProjects(
                "  Roadmap  ", ProjectStatus.CREATED, 7L, pageable);

        assertEquals(expected, result);
        verify(projectRepository).findVisibleProjects(
                "Roadmap", ProjectStatus.CREATED, 7L, pageable);
    }

    @Test
    void getSubProjectsNormalizesBlankNameAndPassesViewerToVisibilityQuery() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Project> expected = Page.empty(pageable);
        when(projectRepository.findVisibleSubProjects(
                5L, null, null, 7L, pageable)).thenReturn(expected);

        Page<Project> result = projectService.getSubProjects(5L, "  ", null, 7L, pageable);

        assertEquals(expected, result);
        verify(projectRepository).findVisibleSubProjects(5L, null, null, 7L, pageable);
    }

    @Test
    void getProjectsByIdsRejectsPartialRepositoryResult() {
        Project first = Project.builder().id(1L).build();
        when(projectRepository.findAllById(java.util.Set.of(1L, 2L)))
                .thenReturn(java.util.List.of(first));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.getProjectsByIds(java.util.List.of(1L, 2L), 7L));

        assertEquals("Projects not found: [2]", exception.getMessage());
        verify(projectAuthorizationService, never()).requireViewAccess(any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getProjectsByIdsAuthorizesEveryCompleteResult() {
        Project first = Project.builder().id(1L).build();
        Project second = Project.builder().id(2L).build();
        when(projectRepository.findAllById(java.util.Set.of(1L, 2L)))
                .thenReturn(java.util.List.of(first, second));

        java.util.List<Project> result = projectService.getProjectsByIds(
                java.util.List.of(1L, 2L, 2L), 7L);

        assertEquals(java.util.List.of(first, second), result);
        verify(projectAuthorizationService).requireViewAccess(first, 7L);
        verify(projectAuthorizationService).requireViewAccess(second, 7L);
    }

    @Test
    void getProjectById_ShouldReturnProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(project.getId());

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectRepository, times(1)).findById(project.getId());
    }

    @Test
    void getProjectById_ShouldThrowExceptionIfNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.getProjectById(project.getId()));

        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    void testUploadProjectCover_Success() throws IOException {
        byte[] mockImageBytes = new byte[]{1, 2, 3};
        byte[] mockResizedImageBytes = new byte[]{4, 5, 6};

        when(file.getBytes()).thenReturn(mockImageBytes);
        when(projectProperties.getTargetWidth()).thenReturn(1080);
        when(projectProperties.getTargetHeight()).thenReturn(566);
        when(imageResizer.resizeImage(mockImageBytes, 1080, 566))
                .thenReturn(mockResizedImageBytes);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(s3Service.uploadFile(any(), anyString())).thenReturn(new Resource());

        projectService.uploadProjectCover(1L, file, 1L);

        verify(fileValidator).validateFile(file);
        verify(imageResizer).resizeImage(mockImageBytes, 1080, 566);
        verify(s3Service).uploadFile(any(), anyString());
        verify(projectRepository).save(project);
    }

    @Test
    void uploadProjectCover_ProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> projectService.uploadProjectCover(1L, file, 1L));
    }

    @Test
    void uploadProjectCover_CoverAlreadyExists() {
        project.setCoverImageId("existing-cover-key");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(IllegalStateException.class, () -> projectService.uploadProjectCover(1L, file, 1L));
    }

    @Test
    void uploadProjectCover_IOException() throws IOException {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getBytes()).thenThrow(IOException.class);

        assertThrows(RuntimeException.class, () -> projectService.uploadProjectCover(1L, file, 1L));
    }

    @Test
    void deleteCover_Success() {
        project.setCoverImageId("cover-key");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.deleteCover(1L, 1L);

        verify(storageTransactionCoordinator).deleteAfterCommit("cover-key");
        assertNull(project.getCoverImageId());
    }

    @Test
    void deleteCover_ProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> projectService.deleteCover(1L, 1L));
    }

    @Test
    void deleteCover_CoverDoesNotExist() {
        project.setCoverImageId(null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(IllegalStateException.class, () -> projectService.deleteCover(1L, 1L));
    }
}
