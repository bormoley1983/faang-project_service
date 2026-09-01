package faang.school.projectservice.service;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import faang.school.projectservice.dto.client.UserDto;
import faang.school.projectservice.model.Schedule;
import faang.school.projectservice.service.google.GoogleCalendarService;
import faang.school.projectservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProjectScheduleService {

    private final GoogleCalendarService googleCalendarService;
    private final UserService userService;

    public void createScheduleEvent(String calendarId, Schedule schedule) {
        Event event = new Event();
        event.setSummary(schedule.getName());
        event.setDescription(schedule.getDescription());
        LocalDateTime startDateTime =schedule.getCreatedAt();
        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone(ZoneId.systemDefault().toString());

        String creatorEmail = getCreatorEmail(schedule);
        event.setCreator(new Event.Creator().setEmail(creatorEmail));

        event.setStart(start);

        googleCalendarService.createEvent(calendarId, event);
    }

    private String getCreatorEmail(Schedule schedule) {
        if (schedule.getProject() == null || schedule.getProject().getOwnerId() == null
                || schedule.getProject().getOwnerId() <= 0) {
            throw new IllegalArgumentException("Schedule project owner must be specified");
        }

        UserDto creator = userService.getUser(schedule.getProject().getOwnerId());
        if (creator == null || creator.getEmail() == null || creator.getEmail().isBlank()) {
            throw new IllegalArgumentException("Schedule creator must have an email");
        }
        return creator.getEmail();
    }

    public Event getScheduleEvent(String calendarId, String eventId) {
        return googleCalendarService.getEvent(calendarId, eventId);
    }

    public void deleteScheduleEvent(String calendarId, String eventId) {
        googleCalendarService.deleteEvent(calendarId, eventId);
    }
}
