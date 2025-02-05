package faang.school.projectservice.service;

import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

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
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project updatedProject = projectService.updateProject(project);

        assertNotNull(updatedProject);
        assertEquals("Test Project", updatedProject.getName());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void updateProject_ShouldThrowExceptionIfNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.updateProject(project));

        assertEquals("Project not found", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
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
}