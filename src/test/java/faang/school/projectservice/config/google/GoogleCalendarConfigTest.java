package faang.school.projectservice.config.google;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleCalendarConfigTest {

    @Test
    void rejectsMissingCredentialsWhenCalendarIsEnabled() {
        GoogleCalendarConfig config = new GoogleCalendarConfig("project-service", "  ");

        assertThrows(IllegalStateException.class, config::calendarService);
    }

    @Test
    void acceptsNormalServiceAccountJsonStructure() {
        String credentials = """
                {
                  "type": "service_account",
                  "client_email": "calendar@example.iam.gserviceaccount.com",
                  "private_key": "-----BEGIN PRIVATE KEY-----\\nkey-data\\n-----END PRIVATE KEY-----\\n"
                }
                """;

        assertDoesNotThrow(() -> GoogleCalendarConfig.validateCredentialsJson(credentials));
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(IllegalArgumentException.class,
                () -> GoogleCalendarConfig.validateCredentialsJson("{not-json}"));
    }

    @Test
    void rejectsCredentialJsonWithoutRequiredServiceAccountFields() {
        String credentials = """
                {"type":"authorized_user","private_key":"not-a-key"}
                """;

        assertThrows(IllegalArgumentException.class,
                () -> GoogleCalendarConfig.validateCredentialsJson(credentials));
    }
}
