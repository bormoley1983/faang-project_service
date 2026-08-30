package faang.school.projectservice.service.google;

import com.google.api.services.calendar.model.Calendar;
import faang.school.projectservice.event.ProjectCalendarProvisioningRequested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCalendarProvisioningListenerTest {

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private ProjectCalendarLinkService calendarLinkService;

    private ProjectCalendarProvisioningListener listener;

    @BeforeEach
    void setUp() {
        listener = new ProjectCalendarProvisioningListener(googleCalendarService, calendarLinkService);
    }

    @Test
    void linksRemoteCalendarAfterItIsCreated() {
        ProjectCalendarProvisioningRequested request = new ProjectCalendarProvisioningRequested(42L, "Roadmap");
        when(googleCalendarService.createCalendar(any(Calendar.class)))
                .thenReturn(Optional.of(new Calendar().setId("calendar-42")));

        listener.provisionCalendar(request);

        verify(calendarLinkService).linkCalendar(42L, "calendar-42");
        verify(googleCalendarService, never()).deleteCalendar(any());
    }

    @Test
    void skipsLinkingWhenCalendarIntegrationIsUnavailable() {
        when(googleCalendarService.createCalendar(any(Calendar.class))).thenReturn(Optional.empty());

        listener.provisionCalendar(new ProjectCalendarProvisioningRequested(42L, "Roadmap"));

        verify(calendarLinkService, never()).linkCalendar(any(), any());
    }

    @Test
    void deletesRemoteCalendarWhenLocalLinkingFails() {
        when(googleCalendarService.createCalendar(any(Calendar.class)))
                .thenReturn(Optional.of(new Calendar().setId("orphan-calendar")));
        doThrow(new IllegalStateException("database unavailable"))
                .when(calendarLinkService).linkCalendar(42L, "orphan-calendar");

        listener.provisionCalendar(new ProjectCalendarProvisioningRequested(42L, "Roadmap"));

        verify(googleCalendarService).deleteCalendar("orphan-calendar");
    }

    @Test
    void doesNotLinkCalendarWhenProviderReturnsNoId() {
        when(googleCalendarService.createCalendar(any(Calendar.class)))
                .thenReturn(Optional.of(new Calendar()));

        listener.provisionCalendar(new ProjectCalendarProvisioningRequested(42L, "Roadmap"));

        verify(calendarLinkService, never()).linkCalendar(any(), any());
    }
}
