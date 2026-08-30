package faang.school.projectservice.service.google;

import com.google.api.services.calendar.model.Calendar;
import faang.school.projectservice.event.ProjectCalendarProvisioningRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCalendarProvisioningListener {

    private final GoogleCalendarService googleCalendarService;
    private final ProjectCalendarLinkService calendarLinkService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void provisionCalendar(ProjectCalendarProvisioningRequested request) {
        Calendar calendarRequest = new Calendar()
                .setSummary(request.projectName())
                .setDescription("Calendar for project: " + request.projectName());

        googleCalendarService.createCalendar(calendarRequest).ifPresent(createdCalendar -> {
            String calendarId = createdCalendar.getId();
            if (calendarId == null || calendarId.isBlank()) {
                log.error("Google Calendar returned no id for project {}", request.projectId());
                return;
            }

            try {
                calendarLinkService.linkCalendar(request.projectId(), calendarId);
            } catch (RuntimeException linkingFailure) {
                googleCalendarService.deleteCalendar(calendarId);
                log.error("Failed to link calendar to project {}; compensation requested",
                        request.projectId(), linkingFailure);
            }
        });
    }
}
