package faang.school.projectservice.service.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import faang.school.projectservice.exception.CalendarIntegrationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GoogleCalendarService}.
 *
 * <p>Each public method has two branches: calendar absent (no-op / null / empty) and
 * calendar present (API call, success or IOException → CalendarIntegrationException).
 */
@ExtendWith(MockitoExtension.class)
class GoogleCalendarServiceTest {

    @Mock
    private Calendar calendar;

    // ── createEvent ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createEvent")
    class CreateEvent {

        @Test
        @DisplayName("returns null when calendar is not configured")
        void returnsNullWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            Event result = svc.createEvent("cal-id", buildEvent());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns the created event on success")
        void returnsCreatedEventOnSuccess() throws IOException {
            Calendar.Events events = mock(Calendar.Events.class);
            Calendar.Events.Insert req = mock(Calendar.Events.Insert.class);
            Event created = buildEvent();
            when(calendar.events()).thenReturn(events);
            when(events.insert(eq("cal-id"), any(Event.class))).thenReturn(req);
            when(req.execute()).thenReturn(created);
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            Event result = svc.createEvent("cal-id", buildEvent());

            assertThat(result).isSameAs(created);
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Events events = mock(Calendar.Events.class);
            Calendar.Events.Insert req = mock(Calendar.Events.Insert.class);
            when(calendar.events()).thenReturn(events);
            when(events.insert(eq("cal-id"), any(Event.class))).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.createEvent("cal-id", buildEvent()))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to create event");
        }
    }

    // ── getEvent ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getEvent")
    class GetEvent {

        @Test
        @DisplayName("returns null when calendar is not configured")
        void returnsNullWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            Event result = svc.getEvent("cal-id", "evt-id");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns the event on success")
        void returnsEventOnSuccess() throws IOException {
            Calendar.Events events = mock(Calendar.Events.class);
            Calendar.Events.Get req = mock(Calendar.Events.Get.class);
            Event existing = buildEvent();
            when(calendar.events()).thenReturn(events);
            when(events.get("cal-id", "evt-id")).thenReturn(req);
            when(req.execute()).thenReturn(existing);
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            Event result = svc.getEvent("cal-id", "evt-id");

            assertThat(result).isSameAs(existing);
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Events events = mock(Calendar.Events.class);
            Calendar.Events.Get req = mock(Calendar.Events.Get.class);
            when(calendar.events()).thenReturn(events);
            when(events.get("cal-id", "evt-id")).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.getEvent("cal-id", "evt-id"))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to get event");
        }
    }

    // ── deleteEvent ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEvent {

        @Test
        @DisplayName("no-op when calendar is not configured")
        void noOpWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            svc.deleteEvent("cal-id", "evt-id");
        }

        @Test
        @DisplayName("delegates to API on success")
        void delegatesOnSuccess() throws IOException {
            Calendar.Events events = mock(Calendar.Events.class);
            Calendar.Events.Delete req = mock(Calendar.Events.Delete.class);
            when(calendar.events()).thenReturn(events);
            when(events.delete("cal-id", "evt-id")).thenReturn(req);
            doNothing().when(req).execute();
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            svc.deleteEvent("cal-id", "evt-id");

            verify(req).execute();
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Events events = mock(Calendar.Events.class);
            Calendar.Events.Delete req = mock(Calendar.Events.Delete.class);
            when(calendar.events()).thenReturn(events);
            when(events.delete("cal-id", "evt-id")).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.deleteEvent("cal-id", "evt-id"))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to delete event");
        }
    }

    // ── createCalendar ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createCalendar")
    class CreateCalendar {

        @Test
        @DisplayName("returns empty Optional when calendar is not configured")
        void returnsEmptyWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            Optional<com.google.api.services.calendar.model.Calendar> result =
                    svc.createCalendar(new com.google.api.services.calendar.model.Calendar());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns the created calendar on success")
        void returnsCreatedCalendarOnSuccess() throws IOException {
            Calendar.Calendars calendars = mock(Calendar.Calendars.class);
            Calendar.Calendars.Insert req = mock(Calendar.Calendars.Insert.class);
            com.google.api.services.calendar.model.Calendar created = new com.google.api.services.calendar.model.Calendar();
            created.setId("cal-123");
            when(calendar.calendars()).thenReturn(calendars);
            when(calendars.insert(any(com.google.api.services.calendar.model.Calendar.class))).thenReturn(req);
            when(req.execute()).thenReturn(created);
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            Optional<com.google.api.services.calendar.model.Calendar> result =
                    svc.createCalendar(new com.google.api.services.calendar.model.Calendar());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("cal-123");
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Calendars calendars = mock(Calendar.Calendars.class);
            Calendar.Calendars.Insert req = mock(Calendar.Calendars.Insert.class);
            when(calendar.calendars()).thenReturn(calendars);
            when(calendars.insert(any(com.google.api.services.calendar.model.Calendar.class))).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.createCalendar(new com.google.api.services.calendar.model.Calendar()))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to create calendar");
        }
    }

    // ── deleteCalendar ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteCalendar")
    class DeleteCalendar {

        @Test
        @DisplayName("returns false when calendar is not configured")
        void returnsFalseWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            boolean result = svc.deleteCalendar("cal-id");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true on success")
        void returnsTrueOnSuccess() throws IOException {
            Calendar.Calendars calendars = mock(Calendar.Calendars.class);
            Calendar.Calendars.Delete req = mock(Calendar.Calendars.Delete.class);
            when(calendar.calendars()).thenReturn(calendars);
            when(calendars.delete("cal-id")).thenReturn(req);
            doNothing().when(req).execute();
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            boolean result = svc.deleteCalendar("cal-id");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Calendars calendars = mock(Calendar.Calendars.class);
            Calendar.Calendars.Delete req = mock(Calendar.Calendars.Delete.class);
            when(calendar.calendars()).thenReturn(calendars);
            when(calendars.delete("cal-id")).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.deleteCalendar("cal-id"))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to delete calendar");
        }
    }

    // ── getCalendar ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCalendar")
    class GetCalendar {

        @Test
        @DisplayName("returns null when calendar is not configured")
        void returnsNullWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            CalendarListEntry result = svc.getCalendar("cal-id");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns the calendar list entry on success")
        void returnsCalendarListEntryOnSuccess() throws IOException {
            Calendar.CalendarList calendarList = mock(Calendar.CalendarList.class);
            Calendar.CalendarList.Get req = mock(Calendar.CalendarList.Get.class);
            CalendarListEntry entry = new CalendarListEntry();
            entry.setId("cal-456");
            when(calendar.calendarList()).thenReturn(calendarList);
            when(calendarList.get("cal-id")).thenReturn(req);
            when(req.execute()).thenReturn(entry);
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            CalendarListEntry result = svc.getCalendar("cal-id");

            assertThat(result).isSameAs(entry);
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.CalendarList calendarList = mock(Calendar.CalendarList.class);
            Calendar.CalendarList.Get req = mock(Calendar.CalendarList.Get.class);
            when(calendar.calendarList()).thenReturn(calendarList);
            when(calendarList.get("cal-id")).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.getCalendar("cal-id"))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to get calendar");
        }
    }

    // ── createAcl ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createAcl")
    class CreateAcl {

        @Test
        @DisplayName("returns false when calendar is not configured")
        void returnsFalseWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            boolean result = svc.createAcl("cal-id", new AclRule());

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true on success")
        void returnsTrueOnSuccess() throws IOException {
            Calendar.Acl acl = mock(Calendar.Acl.class);
            Calendar.Acl.Insert req = mock(Calendar.Acl.Insert.class);
            when(calendar.acl()).thenReturn(acl);
            when(acl.insert(eq("cal-id"), any(AclRule.class))).thenReturn(req);
            when(req.execute()).thenReturn(new AclRule());
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            boolean result = svc.createAcl("cal-id", new AclRule());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Acl acl = mock(Calendar.Acl.class);
            Calendar.Acl.Insert req = mock(Calendar.Acl.Insert.class);
            when(calendar.acl()).thenReturn(acl);
            when(acl.insert(eq("cal-id"), any(AclRule.class))).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.createAcl("cal-id", new AclRule()))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to create ACL");
        }
    }

    // ── getAcl ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAcl")
    class GetAcl {

        @Test
        @DisplayName("returns null when calendar is not configured")
        void returnsNullWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            AclRule result = svc.getAcl("cal-id", "rule-id");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns the ACL rule on success")
        void returnsAclRuleOnSuccess() throws IOException {
            Calendar.Acl acl = mock(Calendar.Acl.class);
            Calendar.Acl.Get req = mock(Calendar.Acl.Get.class);
            AclRule rule = new AclRule();
            rule.setId("rule-1");
            when(calendar.acl()).thenReturn(acl);
            when(acl.get("cal-id", "rule-id")).thenReturn(req);
            when(req.execute()).thenReturn(rule);
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            AclRule result = svc.getAcl("cal-id", "rule-id");

            assertThat(result).isSameAs(rule);
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Acl acl = mock(Calendar.Acl.class);
            Calendar.Acl.Get req = mock(Calendar.Acl.Get.class);
            when(calendar.acl()).thenReturn(acl);
            when(acl.get("cal-id", "rule-id")).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.getAcl("cal-id", "rule-id"))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to get ACL");
        }
    }

    // ── deleteAcl ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteAcl")
    class DeleteAcl {

        @Test
        @DisplayName("no-op when calendar is not configured")
        void noOpWhenCalendarAbsent() {
            GoogleCalendarService svc = new GoogleCalendarService(Optional.empty());

            svc.deleteAcl("cal-id", "rule-id");
        }

        @Test
        @DisplayName("delegates to API on success")
        void delegatesOnSuccess() throws IOException {
            Calendar.Acl acl = mock(Calendar.Acl.class);
            Calendar.Acl.Delete req = mock(Calendar.Acl.Delete.class);
            when(calendar.acl()).thenReturn(acl);
            when(acl.delete("cal-id", "rule-id")).thenReturn(req);
            doNothing().when(req).execute();
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            svc.deleteAcl("cal-id", "rule-id");

            verify(req).execute();
        }

        @Test
        @DisplayName("throws CalendarIntegrationException on IOException")
        void throwsOnIOException() throws IOException {
            Calendar.Acl acl = mock(Calendar.Acl.class);
            Calendar.Acl.Delete req = mock(Calendar.Acl.Delete.class);
            when(calendar.acl()).thenReturn(acl);
            when(acl.delete("cal-id", "rule-id")).thenReturn(req);
            when(req.execute()).thenThrow(new IOException("boom"));
            GoogleCalendarService svc = new GoogleCalendarService(Optional.of(calendar));

            assertThatThrownBy(() -> svc.deleteAcl("cal-id", "rule-id"))
                    .isInstanceOf(CalendarIntegrationException.class)
                    .hasMessageContaining("Failed to delete ACL");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Event buildEvent() {
        Event event = new Event();
        event.setSummary("Test Event");
        return event;
    }
}
