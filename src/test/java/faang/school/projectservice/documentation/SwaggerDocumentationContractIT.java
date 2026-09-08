package faang.school.projectservice.documentation;

import faang.school.projectservice.ProjectServiceApplication;
import faang.school.projectservice.config.TestContainersConfig;
import faang.school.projectservice.config.TestGoogleCalendarConfig;
import faang.school.projectservice.config.TestS3Config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract test for the service's interactive API documentation (Swagger UI).
 *
 * <p>All faang services standardize on springdoc's default Swagger UI path
 * {@code /swagger-ui/index.html} and the machine-readable OpenAPI document at
 * {@code /v3/api-docs}. This test pins that contract so an accidental config change or a
 * regression to the Spring Boot fallback 404 page is caught.
 */
@Tag("integration")
@SpringBootTest(classes = {
    ProjectServiceApplication.class,
    TestContainersConfig.class,
    TestS3Config.class,
    TestGoogleCalendarConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerDocumentationContractIT {

    /**
     * The test profile uses {@code ddl-auto: update} against the fresh Testcontainers database,
     * but the main config's default schema ({@code project_service}) does not exist there. Clearing
     * the default schema lets Hibernate create tables in the public schema so the context boots.
     */
    @DynamicPropertySource
    static void clearDefaultSchema(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiPage_ShouldServeSwaggerUi() throws Exception {
        // The standardized default Swagger UI path serves the actual bootstrap page (not the Spring Boot 404).
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("swagger-ui")));
    }

    @Test
    void apiDocs_ShouldServeOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").isNotEmpty())
                .andExpect(jsonPath("$.info.title").value("Project microservice API documentation"));
    }
}
