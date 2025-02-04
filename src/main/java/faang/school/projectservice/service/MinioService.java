package faang.school.projectservice.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioService(@Value("${services.minio.endpoint}") String endpoint,
                        @Value("${services.minio.access-key}") String accessKey,
                        @Value("${services.minio.secret-key}") String secretKey,
                        @Value("${services.minio.bucket-name}") String bucketName) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
    }

    public void uploadPdfToMinio(InputStream pdfFile, String fileName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(pdfFile, -1, 5 * 1024 * 1024)
                            .build()
            );
            log.info("File successfully uploaded to Minio. {}", fileName);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to upload file to Minio", e);
        }
        if (pdfFile != null) {
            try {
                pdfFile.close();
            } catch (IOException e) {
                log.error("Failed to close pdfFile InputStream: {}", e.getMessage());
            }
        }
    }

    public String generatePresignedUrl(String fileName) {
        try {
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .method(Method.GET)
                    .expiry(1, TimeUnit.DAYS)
                    .build();
            return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate presigned url", e);
        }
    }

    public void removePdfFromMinio(String fileName) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            if (statObjectResponse != null) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .build()
                );
                log.info("Old file deleted from Minio: {}", fileName);
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.warn("Error deleting file {} from Minio {}: ", fileName, e.getMessage());
        }
    }
}
