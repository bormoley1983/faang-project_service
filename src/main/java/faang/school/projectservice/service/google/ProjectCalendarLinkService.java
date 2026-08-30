package faang.school.projectservice.service.google;

import faang.school.projectservice.model.Project;
import faang.school.projectservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectCalendarLinkService {

    private final ProjectRepository projectRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void linkCalendar(Long projectId, String calendarId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("Project no longer exists: " + projectId));
        project.setGoogleCalendarId(calendarId);
        projectRepository.save(project);
    }
}
