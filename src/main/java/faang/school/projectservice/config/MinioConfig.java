package faang.school.projectservice.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${services.s3.endpoint}") String endpoint,
            @Value("${services.s3.access-key}") String accessKey,
            @Value("${services.s3.secret-key}") String secretKey
    ) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Value("${services.s3.bucket-name}")
    private String bucketName;

    @Bean
    public String minioBucketName() {
        return bucketName;
    }
}
