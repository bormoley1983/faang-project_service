package faang.school.projectservice.service;

import faang.school.projectservice.exseption.ErrorReadingFile;
import faang.school.projectservice.exseption.ProjectNotFoundException;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectReport;
import faang.school.projectservice.repository.ProjectReportRepository;
import faang.school.projectservice.repository.ProjectRepository;
import faang.school.projectservice.service.s3.S3Service;
import faang.school.projectservice.service.s3.StorageTransactionCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class ProjectReportService {
    private final S3Service s3Service;
    private final StorageTransactionCoordinator storageTransactionCoordinator;
    private final ProjectReportRepository projectReportRepository;
    private final PdfReportGeneratorService pdfReportGeneratorService;
    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectReport createProjectReport(Long projectId) {
        String fileName = "project_report_" + projectId + ".pdf";
        ProjectReport projectReport = projectReportRepository.getReportByProjectId(projectId)
                .orElseGet(ProjectReport::new);
        String oldKey = projectReport.getFileKey();
        Resource pdfResource = generateAndUploadProjectReport(projectId);
        String fileKey;
        try {
            fileKey = s3Service.uploadFile(pdfResource.getInputStream(), pdfResource.contentLength(),
                    "application/pdf", fileName, "reports");
        } catch (IOException e) {
            throw new RuntimeException("Error reading PDF for project " + projectId, e);
        }
        storageTransactionCoordinator.deleteOnRollback(fileKey);

        projectReport.setProjectId(projectId);
        projectReport.setFileKey(fileKey);
        projectReport.setFileUrl(null);

        projectReportRepository.save(projectReport);
        if (oldKey != null) {
            storageTransactionCoordinator.deleteAfterCommit(oldKey);
        }
        return projectReport;
    }

    private Resource generateAndUploadProjectReport(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        return pdfReportGeneratorService.generateProjectReport(project);
    }

    public ProjectReport getProjectReport(Long projectId) {
        return projectReportRepository.getReportByProjectId(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project report not found"));
    }

    public Resource getUrlResource(ProjectReport projectReport) {
        return s3Service.downloadFile(projectReport.getFileKey()).getInputStreamResource();
    }

    public long getContentLength(Resource resource) {
        try {
            return resource.contentLength();
        } catch (IOException e) {
            throw new ErrorReadingFile("Error reading content length", e);
        }
    }
}
