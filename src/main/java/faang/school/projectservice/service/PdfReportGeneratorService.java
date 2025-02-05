package faang.school.projectservice.service;

import faang.school.projectservice.client.UserServiceClient;
import faang.school.projectservice.dto.client.UserDto;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.Task;
import faang.school.projectservice.model.TaskStatus;
import faang.school.projectservice.model.Team;
import faang.school.projectservice.model.TeamMember;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PdfReportGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(PdfReportGeneratorService.class);
    private final UserServiceClient userServiceClient;

    public Resource generateProjectReport(Project project) {
        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                yPosition = drawTextLine(contentStream, margin, yPosition, "Project Presentation",
                        PDType1Font.HELVETICA_BOLD, 20);
                yPosition -= 10;

                yPosition = drawTextLine(contentStream, margin, yPosition, "General Information",
                        PDType1Font.HELVETICA_BOLD, 16);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition, "Project Name: " + project.getName(),
                        PDType1Font.HELVETICA, 12);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition,
                        "Description: " + project.getDescription(), PDType1Font.HELVETICA, 12);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition,
                        "Created At: " + project.getCreatedAt().toString(), PDType1Font.HELVETICA, 12);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition, "Status: " + project.getStatus(),
                        PDType1Font.HELVETICA, 12);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition,
                        "Owner: " + getOwnerName(project.getOwnerId()), PDType1Font.HELVETICA, 12);
                yPosition -= 10;

                yPosition = drawTextLine(contentStream, margin, yPosition, "Project Team", PDType1Font.HELVETICA_BOLD,
                        16);
                for (Team team : project.getTeams()) {
                    yPosition = drawTextLine(contentStream, margin + 10, yPosition, "Team " + team.getId() + ":",
                            PDType1Font.HELVETICA_BOLD, 12);
                    for (TeamMember member : team.getTeamMembers()) {
                        yPosition = drawTextLine(contentStream, margin + 20, yPosition,
                                member.getNickname() + " - " + member.getRoles(), PDType1Font.HELVETICA, 12);
                    }
                    yPosition -= 5;
                }
                yPosition -= 10;

                yPosition = drawTextLine(contentStream, margin, yPosition, "Achievements", PDType1Font.HELVETICA_BOLD,
                        16);
                List<Task> completedTasks = project.getTasks().stream()
                        .filter(task -> task.getStatus().equals(TaskStatus.DONE))
                        .toList();
                if (completedTasks.isEmpty()) {
                    yPosition = drawTextLine(contentStream, margin + 10, yPosition, "No achievements recorded.",
                            PDType1Font.HELVETICA, 12);
                } else {
                    for (Task task : completedTasks) {
                        String taskLine = "- " + task.getName();
                        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                            taskLine += " (" + task.getDescription() + ")";
                        }
                        yPosition = drawTextLine(contentStream, margin + 10, yPosition, taskLine, PDType1Font.HELVETICA,
                                12);
                    }
                }
                yPosition -= 10;

                yPosition = drawTextLine(contentStream, margin, yPosition, "Statistics", PDType1Font.HELVETICA_BOLD,
                        16);
                int totalCompletedTasks = completedTasks.size();
                int totalTeams = project.getTeams().size();
                int totalTeamMembers = project.getTeams().stream().mapToInt(team -> team.getTeamMembers().size()).sum();
                yPosition = drawTextLine(contentStream, margin + 10, yPosition,
                        "Completed Tasks: " + totalCompletedTasks, PDType1Font.HELVETICA, 12);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition, "Number of Teams: " + totalTeams,
                        PDType1Font.HELVETICA, 12);
                yPosition = drawTextLine(contentStream, margin + 10, yPosition,
                        "Total Team Members: " + totalTeamMembers, PDType1Font.HELVETICA, 12);
                yPosition -= 10;

                yPosition = drawTextLine(contentStream, margin, yPosition, "Related Projects",
                        PDType1Font.HELVETICA_BOLD, 16);
                if (project.getParentProject() != null) {
                    yPosition = drawTextLine(contentStream, margin + 10, yPosition,
                            "Parent Project: " + project.getParentProject().getName(), PDType1Font.HELVETICA, 12);
                } else {
                    yPosition = drawTextLine(contentStream, margin + 10, yPosition, "No Parent Project",
                            PDType1Font.HELVETICA, 12);
                }
                if (project.getChildren() != null && !project.getChildren().isEmpty()) {
                    yPosition = drawTextLine(contentStream, margin + 10, yPosition, "Child Projects:",
                            PDType1Font.HELVETICA, 12);
                    for (Project child : project.getChildren()) {
                        yPosition = drawTextLine(contentStream, margin + 20, yPosition, "- " + child.getName(),
                                PDType1Font.HELVETICA, 12);
                    }
                } else {
                    yPosition = drawTextLine(contentStream, margin + 10, yPosition, "No Child Projects",
                            PDType1Font.HELVETICA, 12);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error generating project report for project " + project.getId(), e);
            }

            ByteArrayOutputStream filePdf = new ByteArrayOutputStream();
            document.save(filePdf);

            return new ByteArrayResource(filePdf.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error saving project report for project " + project.getId(), e);
        }
    }

    private String getOwnerName(Long ownerId) {
        log.info("Get owner name: {}", ownerId);
        if (ownerId == null) {
            log.warn("Owner ID is null");
            throw new IllegalArgumentException("Owner ID cannot be null");
        }

        try {
            UserDto user = userServiceClient.getUser(ownerId);

            if (user == null || user.username() == null) {
                log.warn("User with ID {} not found or has no username", ownerId);
                return "Unknown User"; // Фоллбек
            }
            return user.username();
        } catch (FeignException.NotFound e) {
            log.warn("User with ID {} not found", ownerId);
            return "Unknown User";
        } catch (Exception e) {
            log.error("Unexpected error while getting owner name for ID {}: {}", ownerId, e.getMessage(), e);
            throw new RuntimeException("Error getting owner name: " + ownerId, e);
        }
    }

    private float drawTextLine(PDPageContentStream contentStream, float x, float y, String text, PDFont font,
                               float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - fontSize - 5;
    }
}
