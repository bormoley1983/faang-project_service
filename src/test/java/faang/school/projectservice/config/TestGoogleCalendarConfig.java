package faang.school.projectservice.config;

import com.google.api.services.calendar.Calendar;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestGoogleCalendarConfig {

    @Bean
    @Primary
    public Calendar calendarService() {
        return mock(Calendar.class);
    }
}