package faang.school.projectservice.controller;

import faang.school.projectservice.model.ProjectReport;
import faang.school.projectservice.service.ProjectReportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ProjectReportController {
    private static final Logger log = LoggerFactory.getLogger(ProjectReportController.class);
    private final ProjectReportService projectReportService;

    @PostMapping("/generate/{projectId}")
    public ResponseEntity<Resource> generateReport(@PathVariable Long projectId) {
        log.info("Generating report for projectId: {}", projectId);

            ProjectReport projectReport = projectReportService.createProjectReport(projectId);
            Resource resource = projectReportService.getUrlResource(projectReport);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: ", e);
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Resource> getReport(@PathVariable Long projectId) {
        log.info("Fetching report for projectId: {}", projectId);

        ProjectReport projectReport = projectReportService.getProjectReport(projectId);
        Resource resource = projectReportService.getUrlResource(projectReport);

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e) {
            log.error("Error reading file for projectId: {}", projectId, e);
            throw new RuntimeException("Error reading file: ", e);
        }
    }
}
