package faang.school.projectservice.service;

import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import faang.school.projectservice.model.Meet;
import faang.school.projectservice.dto.client.UserDto;
import faang.school.projectservice.service.google.GoogleCalendarService;
import faang.school.projectservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMeetService {
    private final GoogleCalendarService googleCalendarService;
    private final UserService userService;

    public void createMeetEvent(String calendarId, Meet meet) {
        validateMeet(calendarId, meet);

        List<Long> requestedUserIds = new java.util.ArrayList<>();
        requestedUserIds.add(meet.getCreatorId());
        if (meet.getUserIds() != null) {
            requestedUserIds.addAll(meet.getUserIds());
        }
        List<Long> distinctUserIds = new LinkedHashSet<>(requestedUserIds).stream().toList();
        Map<Long, UserDto> usersById = userService.getUsers(distinctUserIds).stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity(), (first, ignored) -> first));
        String creatorEmail = requireUserEmail(usersById, meet.getCreatorId());
        List<String> attendeesEmail = distinctUserIds.stream()
                .filter(userId -> userId != meet.getCreatorId())
                .map(userId -> requireUserEmail(usersById, userId))
                .toList();

        Event event = new Event();
        event.setSummary(meet.getTitle());
        event.setDescription(meet.getDescription());

        LocalDateTime startDateTime = meet.getStartsAt();
        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone(ZoneId.systemDefault().toString());
        event.setStart(start);
        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        meet.getEndsAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone(ZoneId.systemDefault().toString());
        event.setEnd(end);

        event.setCreator(new Event.Creator().setEmail(creatorEmail));
        List<EventAttendee> attendees = attendeesEmail.stream()
                .map(email -> new EventAttendee().setEmail(email))
                .collect(Collectors.toList());
        event.setAttendees(attendees);

        Event createdEvent = googleCalendarService.createEvent(calendarId, event);
        if (createdEvent == null || createdEvent.getId() == null || createdEvent.getId().isBlank()) {
            throw new IllegalStateException("Google Calendar did not create the meet event");
        }
        meet.setGoogleEventId(createdEvent.getId());

        try {
            createAclForEvent(calendarId, creatorEmail, attendeesEmail);
        } catch (RuntimeException aclFailure) {
            googleCalendarService.deleteEvent(calendarId, createdEvent.getId());
            meet.setGoogleEventId(null);
            throw aclFailure;
        }
    }

    public Event getMeetEvent(String calendarId, String eventId) {
        return googleCalendarService.getEvent(calendarId, eventId);
    }

    public void deleteMeetEvent(String calendarId, String eventId) {
        googleCalendarService.deleteEvent(calendarId, eventId);
    }

    private void createAclForEvent(String calendarId, String creatorEmail, List<String> attendeesEmail) {
        AclRule creatorAcl = new AclRule()
                .setRole("owner")
                .setScope(new Scope().setType("user").setValue(creatorEmail));
        if (!googleCalendarService.createAcl(calendarId, creatorAcl)) {
            throw new IllegalStateException("Failed to create Calendar ACL for meet creator");
        }

        attendeesEmail.forEach(participantEmail -> {
            AclRule attendeeAcl = new AclRule()
                    .setRole("reader")
                    .setScope(new Scope().setType("user").setValue(participantEmail));
            if (!googleCalendarService.createAcl(calendarId, attendeeAcl)) {
                throw new IllegalStateException("Failed to create Calendar ACL for meet attendee");
            }
        });
    }

    private void validateMeet(String calendarId, Meet meet) {
        if (calendarId == null || calendarId.isBlank()) {
            throw new IllegalArgumentException("Calendar id must not be blank");
        }
        if (meet == null || meet.getTitle() == null || meet.getTitle().isBlank()) {
            throw new IllegalArgumentException("Meet title must not be blank");
        }
        if (meet.getStartsAt() == null || meet.getEndsAt() == null
                || !meet.getEndsAt().isAfter(meet.getStartsAt())) {
            throw new IllegalArgumentException("Meet end time must be after its start time");
        }
        if (meet.getCreatorId() <= 0) {
            throw new IllegalArgumentException("Meet creator id must be positive");
        }
    }

    private String requireUserEmail(Map<Long, UserDto> usersById, long userId) {
        UserDto user = usersById.get(userId);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Meet user has no valid email: " + userId);
        }
        return user.getEmail();
    }
}
