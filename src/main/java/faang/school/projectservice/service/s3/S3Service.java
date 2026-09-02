package faang.school.projectservice.service.s3;

import faang.school.projectservice.dto.resource.S3FileDto;
import faang.school.projectservice.exception.FileException;
import faang.school.projectservice.model.Resource;
import faang.school.projectservice.model.ResourceStatus;
import faang.school.projectservice.model.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.math.BigInteger;
import java.util.UUID;
import java.io.InputStream;

import static faang.school.projectservice.service.s3.S3ErrorMessage.FAILED_DELETE_FILE;
import static faang.school.projectservice.service.s3.S3ErrorMessage.FAILED_DOWNLOAD_FILE;
import static faang.school.projectservice.service.s3.S3ErrorMessage.FAILED_UPLOAD_FILE;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private static final String KEY_PATTERN = "%s/%s_%s";

    private final S3Client s3Client;

    @Value("${services.s3.bucket-name}")
    private String bucketName;

    public Resource uploadFile(MultipartFile file, String folder) {
        long fileSize = file.getSize();
        String fileType = file.getContentType();
        String fileName = file.getOriginalFilename();

        String key = String.format(KEY_PATTERN, folder,
                UUID.randomUUID(), fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.info("File uploaded successfully");
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: ", e);
            throw new FileException(FAILED_UPLOAD_FILE);
        } catch (Exception e) {
            log.error("Unexpected error occurred while uploading file: ", e);
            throw new FileException(FAILED_UPLOAD_FILE);
        }

        Resource resource = new Resource();
        resource.setKey(key);
        resource.setName(fileName);
        resource.setSize(BigInteger.valueOf(fileSize));
        resource.setType(ResourceType.getResourceType(fileType));
        resource.setStatus(ResourceStatus.ACTIVE);

        return resource;
    }

    public String uploadFile(InputStream inputStream, long size, String contentType, String fileName, String folder) {
        String key = String.format(KEY_PATTERN, folder, UUID.randomUUID(), fileName);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName).key(key).contentType(contentType).contentLength(size).build();
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, size));
            return key;
        } catch (Exception exception) {
            log.error("Failed to upload generated file to S3", exception);
            throw new FileException(FAILED_UPLOAD_FILE);
        }
    }

    public S3FileDto downloadFile(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            var response = s3Client.getObject(getObjectRequest);
            InputStreamResource inputStream = new InputStreamResource(response);

            S3FileDto file = new S3FileDto();
            file.setFileName(response.response().metadata().getOrDefault("filename", key));
            file.setContentType(response.response().contentType());
            file.setContentLength(response.response().contentLength());
            file.setInputStreamResource(inputStream);
            return file;
        } catch (S3Exception e) {
            log.error("Failed to download file from S3: ", e);
            throw new FileException(FAILED_DOWNLOAD_FILE);
        } catch (Exception e) {
            log.error("Unexpected error occurred while downloading file: ", e);
            throw new FileException(FAILED_DOWNLOAD_FILE);
        }
    }
    
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", key);
        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", key, e);
            throw new FileException(FAILED_DELETE_FILE);
        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting file: ", e);
            throw new FileException(FAILED_DELETE_FILE);
        }
    }
}
