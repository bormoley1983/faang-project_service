package faang.school.projectservice.service.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.CalendarListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import faang.school.projectservice.exception.CalendarIntegrationException;

@Service
public class GoogleCalendarService {
    private final Optional<Calendar> calendar;

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);

    public GoogleCalendarService(Optional<Calendar> calendar) {
        this.calendar = calendar;
        if (calendar.isEmpty()) {
            logger.warn("Google Calendar service is not available - credentials not configured");
        }
    }

    public Event createEvent(String calendarId, Event event) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot create event - Google Calendar service not available");
            return null;
        }
        try {
            return calendar.get().events().insert(calendarId, event).execute();
        } catch (IOException e) {
            throw failure("create event in calendar " + calendarId, e);
        }
    }

    public Event getEvent(String calendarId, String eventId) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot get event - Google Calendar service not available");
            return null;
        }
        try {
            return calendar.get().events().get(calendarId, eventId).execute();
        } catch (IOException e) {
            throw failure("get event " + eventId + " from calendar " + calendarId, e);
        }
    }

    public void deleteEvent(String calendarId, String eventId) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot delete event - Google Calendar service not available");
            return;
        }
        try {
            calendar.get().events().delete(calendarId, eventId).execute();
        } catch (IOException e) {
            throw failure("delete event " + eventId + " from calendar " + calendarId, e);
        }
    }

    public Optional<com.google.api.services.calendar.model.Calendar> createCalendar(
            com.google.api.services.calendar.model.Calendar calendarToCreate) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot create calendar - Google Calendar service not available");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(calendar.get().calendars().insert(calendarToCreate).execute());
        } catch (IOException e) {
            throw failure("create calendar", e);
        }
    }

    public boolean deleteCalendar(String calendarId) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot delete calendar - Google Calendar service not available");
            return false;
        }
        try {
            calendar.get().calendars().delete(calendarId).execute();
            return true;
        } catch (IOException e) {
            throw failure("delete calendar " + calendarId, e);
        }
    }

    public CalendarListEntry getCalendar(String calendarId) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot get calendar - Google Calendar service not available");
            return null;
        }
        try {
            return calendar.get().calendarList().get(calendarId).execute();
        } catch (IOException e) {
            throw failure("get calendar " + calendarId, e);
        }
    }

    public boolean createAcl(String calendarId, com.google.api.services.calendar.model.AclRule aclRule) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot create ACL - Google Calendar service not available");
            return false;
        }
        try {
            calendar.get().acl().insert(calendarId, aclRule).execute();
            return true;
        } catch (IOException e) {
            throw failure("create ACL for calendar " + calendarId, e);
        }
    }

    public com.google.api.services.calendar.model.AclRule getAcl(String calendarId, String ruleId) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot get ACL - Google Calendar service not available");
            return null;
        }
        try {
            return calendar.get().acl().get(calendarId, ruleId).execute();
        } catch (IOException e) {
            throw failure("get ACL " + ruleId + " for calendar " + calendarId, e);
        }
    }

    public void deleteAcl(String calendarId, String ruleId) {
        if (calendar.isEmpty()) {
            logger.warn("Cannot delete ACL - Google Calendar service not available");
            return;
        }
        try {
            calendar.get().acl().delete(calendarId, ruleId).execute();
        } catch (IOException e) {
            throw failure("delete ACL " + ruleId + " for calendar " + calendarId, e);
        }
    }

    private CalendarIntegrationException failure(String operation, IOException cause) {
        logger.error("Google Calendar operation failed: {}", operation, cause);
        return new CalendarIntegrationException("Failed to " + operation, cause);
    }
}
