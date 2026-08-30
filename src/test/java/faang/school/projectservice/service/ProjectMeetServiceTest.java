package faang.school.projectservice.service;

import com.google.api.services.calendar.model.Event;
import faang.school.projectservice.dto.client.UserDto;
import faang.school.projectservice.model.Meet;
import faang.school.projectservice.repository.MeetRepository;
import faang.school.projectservice.service.google.GoogleCalendarService;
import faang.school.projectservice.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMeetServiceTest {

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private UserService userService;

    @Mock
    private MeetRepository meetRepository;

    @InjectMocks
    private ProjectMeetService projectMeetService;

    @Test
    void testCreateMeetEvent() {
        String calendarId = "testCalendarId";
        Meet meet = new Meet();
        meet.setTitle("Test Meet");
        meet.setDescription("Test Description");
        meet.setCreatorId(10L);
        meet.setUserIds(List.of(20L));
        meet.setStartsAt(LocalDateTime.of(2026, 8, 30, 10, 0));
        meet.setEndsAt(LocalDateTime.of(2026, 8, 30, 11, 0));

        Event createdEvent = new Event();
        createdEvent.setId("testEventId");
        when(userService.getUsers(List.of(10L, 20L))).thenReturn(List.of(
                new UserDto(10L, "creator", "creator@example.com"),
                new UserDto(20L, "attendee", "attendee@example.com")));
        when(googleCalendarService.createEvent(anyString(), any(Event.class))).thenReturn(createdEvent);
        when(googleCalendarService.createAcl(anyString(), any())).thenReturn(true);

        projectMeetService.createMeetEvent(calendarId, meet);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(googleCalendarService).createEvent(eq(calendarId), eventCaptor.capture());
        Event event = eventCaptor.getValue();

        assertEquals("Test Meet", event.getSummary());
        assertEquals("Test Description", event.getDescription());
        assertNotNull(event.getStart());
        assertNotNull(event.getEnd());
        assertEquals("creator@example.com", event.getCreator().getEmail());
        assertEquals("attendee@example.com", event.getAttendees().getFirst().getEmail());
        assertEquals("testEventId", meet.getGoogleEventId());
    }

    @Test
    void createMeetEventRejectsInvalidTimeRangeBeforeCallingUsersOrCalendar() {
        Meet meet = validMeet();
        meet.setEndsAt(meet.getStartsAt());

        assertThrows(IllegalArgumentException.class,
                () -> projectMeetService.createMeetEvent("calendar-id", meet));

        verify(userService, never()).getUsers(any());
        verify(googleCalendarService, never()).createEvent(anyString(), any());
    }

    @Test
    void createMeetEventRejectsParticipantWithoutEmail() {
        Meet meet = validMeet();
        when(userService.getUsers(List.of(10L, 20L))).thenReturn(List.of(
                new UserDto(10L, "creator", "creator@example.com"),
                new UserDto(20L, "attendee", " ")));

        assertThrows(IllegalArgumentException.class,
                () -> projectMeetService.createMeetEvent("calendar-id", meet));

        verify(googleCalendarService, never()).createEvent(anyString(), any());
    }

    @Test
    void createMeetEventHandlesMissingProviderResultWithoutDereference() {
        Meet meet = validMeet();
        stubMeetUsers();
        when(googleCalendarService.createEvent(anyString(), any(Event.class))).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> projectMeetService.createMeetEvent("calendar-id", meet));

        assertNull(meet.getGoogleEventId());
    }

    @Test
    void createMeetEventDeletesRemoteEventWhenAclCreationFails() {
        Meet meet = validMeet();
        stubMeetUsers();
        when(googleCalendarService.createEvent(anyString(), any(Event.class)))
                .thenReturn(new Event().setId("event-to-compensate"));
        when(googleCalendarService.createAcl(anyString(), any())).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> projectMeetService.createMeetEvent("calendar-id", meet));

        verify(googleCalendarService).deleteEvent("calendar-id", "event-to-compensate");
        assertNull(meet.getGoogleEventId());
    }

    @Test
    void testGetMeetEvent() {
        String calendarId = "testCalendarId";
        String eventId = "testEventId";
        Event event = new Event();
        when(googleCalendarService.getEvent(calendarId, eventId)).thenReturn(event);

        Event result = projectMeetService.getMeetEvent(calendarId, eventId);

        assertEquals(event, result);
        verify(googleCalendarService).getEvent(calendarId, eventId);
    }

    @Test
    void testDeleteMeetEvent() {
        String calendarId = "testCalendarId";
        String eventId = "testEventId";

        projectMeetService.deleteMeetEvent(calendarId, eventId);

        verify(googleCalendarService).deleteEvent(calendarId, eventId);
    }

    private Meet validMeet() {
        Meet meet = new Meet();
        meet.setTitle("Test Meet");
        meet.setDescription("Test Description");
        meet.setCreatorId(10L);
        meet.setUserIds(List.of(20L));
        meet.setStartsAt(LocalDateTime.of(2026, 8, 30, 10, 0));
        meet.setEndsAt(LocalDateTime.of(2026, 8, 30, 11, 0));
        return meet;
    }

    private void stubMeetUsers() {
        when(userService.getUsers(List.of(10L, 20L))).thenReturn(List.of(
                new UserDto(10L, "creator", "creator@example.com"),
                new UserDto(20L, "attendee", "attendee@example.com")));
    }
}
