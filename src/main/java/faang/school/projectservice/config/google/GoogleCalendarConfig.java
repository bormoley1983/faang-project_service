package faang.school.projectservice.config.google;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@Profile("!test")
public class GoogleCalendarConfig {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarConfig.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    private final String applicationName;
    private final String credentialsJson;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public GoogleCalendarConfig(
            @Value("${google.calendar.application-name}") String applicationName,
            @Value("${google.calendar.credentials-json:}") String credentialsJson) {
        this.applicationName = applicationName;
        this.credentialsJson = credentialsJson;
    }

    @Bean
    @ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
    public Calendar calendarService() {
        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalStateException(
                    "Google Calendar is enabled but service-account credentials are not configured");
        }

        try {
            validateCredentialsJson(credentialsJson);
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                    .createScoped(Collections.singleton(CALENDAR_SCOPE));
            return new Calendar.Builder(new com.google.api.client.http.javanet.NetHttpTransport(),
                    JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Failed to initialize Google Calendar service with configured credentials", e);
            throw new IllegalStateException("Google Calendar service-account credentials are invalid", e);
        }
    }

    static void validateCredentialsJson(String credentialsJson) {
        try {
            JsonNode credentials = OBJECT_MAPPER.readTree(credentialsJson);
            requireText(credentials, "type", "service_account");
            requireText(credentials, "client_email", null);
            String privateKey = requireText(credentials, "private_key", null);
            if (!privateKey.contains("-----BEGIN PRIVATE KEY-----")
                    || !privateKey.contains("-----END PRIVATE KEY-----")) {
                throw new IllegalArgumentException("Google credentials private_key is not a PEM private key");
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Google credentials are not valid JSON", e);
        }
    }

    private static String requireText(JsonNode credentials, String field, String expectedValue) {
        if (credentials == null || !credentials.isObject()) {
            throw new IllegalArgumentException("Google credentials must be a JSON object");
        }
        JsonNode value = credentials.get(field);
        if (value == null || !value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalArgumentException("Google credentials field is missing or blank: " + field);
        }
        if (expectedValue != null && !expectedValue.equals(value.textValue())) {
            throw new IllegalArgumentException("Google credentials field has an unsupported value: " + field);
        }
        return value.textValue();
    }
}
