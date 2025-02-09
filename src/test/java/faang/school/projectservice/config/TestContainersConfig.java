package faang.school.projectservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;


@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:13.3")
                .withReuse(true);
        container.start();
        return container;
    }
}
