package faang.school.projectservice.config.google;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
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

    @Value("${google.calendar.application-name}")
    private String applicationName;

    @Value("${google.calendar.credentials-json:}")
    private String credentialsJson;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    @ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
    public Calendar calendarService() throws IOException {
        if (credentialsJson == null || credentialsJson.trim().isEmpty() || !isValidCredentials()) {
            logger.warn("Google Calendar credentials not configured or invalid. Calendar service will not be available.");
            return null;
        }

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));
            return new Calendar.Builder(new com.google.api.client.http.javanet.NetHttpTransport(),
                    JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
        } catch (IOException e) {
            logger.error("Failed to initialize Google Calendar service with provided credentials", e);
            throw e;
        }
    }

    private boolean isValidCredentials() {
        try {
            return credentialsJson.contains("\"private_key\"") && 
                   !credentialsJson.contains("private_key\":");
        } catch (Exception e) {
            return false;
        }
    }
}