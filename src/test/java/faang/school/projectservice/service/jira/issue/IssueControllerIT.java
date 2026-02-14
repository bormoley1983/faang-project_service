package faang.school.projectservice.service.jira.issue;

import faang.school.projectservice.ProjectServiceApplication;
import faang.school.projectservice.config.TestContainersConfig;
import faang.school.projectservice.config.TestGoogleCalendarConfig;
import faang.school.projectservice.config.TestS3Config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = {
    ProjectServiceApplication.class,
    TestContainersConfig.class,
    TestS3Config.class,
    TestGoogleCalendarConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final String projectKey = "testProjectKey";
    private final String username = "testUsername";
    private final String password = "testPassword";
    private final String baseUrl = "testBaseUrl";

    @Test
    @Disabled("Requires Jira service running")
    void testCreateIssue_shouldReturnValidData() throws Exception {
        String requestBody = """
            {
                "typeId": 10005,
                "summary": "Some summary"
            }
            """;

        mockMvc.perform(post("/jira/issue/" + projectKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-jira-username", username)
                        .header("x-jira-password", password)
                        .header("x-jira-base-url", baseUrl)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeId").value(10005))
                .andExpect(jsonPath("$.summary").value("Some summary"));
    }
}
