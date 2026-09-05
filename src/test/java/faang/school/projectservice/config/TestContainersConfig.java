package faang.school.projectservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;


@TestConfiguration
public class TestContainersConfig {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");
    private static final DockerImageName SEAWEEDFS_IMAGE = DockerImageName.parse(
        "chrislusf/seaweedfs:4.45@sha256:fc9f76fa993ad69966ffeb2f65d0318fcae39c6f8e20cf68ef7b3a5cb97769e5");

    static Network testNetwork = Network.newNetwork();
    private static final GenericContainer<?> SEAWEEDFS_S3_CONTAINER = new GenericContainer<>(SEAWEEDFS_IMAGE)
        .withNetwork(testNetwork)
        .withNetworkAliases("test-s3")
        .withEnv("AWS_ACCESS_KEY_ID", "test-access-key")
        .withEnv("AWS_SECRET_ACCESS_KEY", "test-secret-key")
        .withEnv("S3_BUCKET", "test-bucket")
        .withCommand("mini", "-dir=/data")
        .withExposedPorts(8333)
        .withReuse(true);

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

    /**
     * SeaweedFS S3-compatible object store for integration tests.
     * Runs in 'mini' mode (master + volume + filer + s3 in one process).
     * S3 API is exposed on port 8333 and its dynamically mapped host port is supplied
     * to Spring through {@code services.s3.endpoint}.
     */
    @Bean
    @SuppressWarnings("resource")
    public static GenericContainer<?> seaweedfsS3Container() {
        return SEAWEEDFS_S3_CONTAINER;
    }

    @DynamicPropertySource
    static void registerSeaweedFsProperties(DynamicPropertyRegistry registry) {
        Startables.deepStart(Stream.of(SEAWEEDFS_S3_CONTAINER)).join();
        registry.add(
            "services.s3.endpoint",
            () -> "http://" + SEAWEEDFS_S3_CONTAINER.getHost() + ":" + SEAWEEDFS_S3_CONTAINER.getMappedPort(8333));
    }
}
