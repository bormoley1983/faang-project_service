package faang.school.projectservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration
public class TestContainersConfig {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");

    static Network testNetwork = Network.newNetwork();

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public static PostgreSQLContainer postgresContainer() {
        PostgreSQLContainer container = new PostgreSQLContainer(POSTGRES_IMAGE)
            .withNetwork(testNetwork)
            .withNetworkAliases("test-postgres")		        
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
        return container;
    }

    // MinIO container for integration tests with isolated network
    /*
    @Bean
    public GenericContainer<?> minioContainer() {
        return new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withNetwork(testNetwork)
            .withNetworkAliases("test-minio")
            .withEnv("MINIO_ROOT_USER", "test-access-key")
            .withEnv("MINIO_ROOT_PASSWORD", "test-secret-key")
            .withCommand("server /data")
            .withExposedPorts(9000)
            .withReuse(true);
    }
    */
}
