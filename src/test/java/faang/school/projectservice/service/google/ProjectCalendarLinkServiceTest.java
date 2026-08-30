package faang.school.projectservice.service.google;

import faang.school.projectservice.model.Project;
import faang.school.projectservice.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCalendarLinkServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectCalendarLinkService linkService;

    @Test
    void linksCalendarInANewTransaction() throws NoSuchMethodException {
        Project project = Project.builder().id(42L).build();
        when(projectRepository.findById(42L)).thenReturn(Optional.of(project));

        linkService.linkCalendar(42L, "calendar-42");

        assertEquals("calendar-42", project.getGoogleCalendarId());
        verify(projectRepository).save(project);

        Method method = ProjectCalendarLinkService.class
                .getMethod("linkCalendar", Long.class, String.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertEquals(Propagation.REQUIRES_NEW, transactional.propagation());
    }

    @Test
    void rejectsLinkingWhenCommittedProjectNoLongerExists() {
        when(projectRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> linkService.linkCalendar(42L, "calendar-42"));

        verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
