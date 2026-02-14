package faang.school.projectservice.service;

import faang.school.projectservice.dto.resource.S3FileDto;
import faang.school.projectservice.exception.FileException;
import faang.school.projectservice.model.Resource;
import faang.school.projectservice.model.ResourceStatus;
import faang.school.projectservice.model.ResourceType;
import faang.school.projectservice.service.s3.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.ResponseInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    // private final String bucketName = "test-bucket";

    private MultipartFile file;
    private String folder;
    private String key;

    @BeforeEach
    void setUp() {
        folder = "test-folder";
        file = mock(MultipartFile.class);
        key = "test-key";
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
    }

    @Test
    void testUploadFile_Success() throws IOException {
        String contentType = "image";
        long fileSize = 500L;
        String fileName = "test.png";
        when(file.getContentType()).thenReturn(contentType);
        when(file.getSize()).thenReturn(fileSize);
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        Resource result = s3Service.uploadFile(file, folder);

        assertNotNull(result);
        assertEquals(BigInteger.valueOf(fileSize), result.getSize());
        assertEquals(ResourceType.IMAGE, result.getType());
        assertEquals(ResourceStatus.ACTIVE, result.getStatus());

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_Throws_WhenS3Exception() throws IOException {
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(500L);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThrows(FileException.class, () -> s3Service.uploadFile(file, folder));

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_Throws_WhenIOException() throws IOException {
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(500L);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getInputStream()).thenThrow(new IOException("IO error"));

        assertThrows(FileException.class, () -> s3Service.uploadFile(file, folder));

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

   @Test
    void testDeleteFile_Success() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        s3Service.deleteFile(key);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteFile_Throws_WhenS3Exception() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThrows(FileException.class, () -> s3Service.deleteFile(key));

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }


    @Test
    void testDownloadFile_success() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("filename", "file.txt");
        
        GetObjectResponse response = GetObjectResponse.builder()
                .contentType("text/plain")
                .contentLength(10L)
                .metadata(metadata)
                .build();

        ResponseInputStream<GetObjectResponse> responseInputStream = 
                new ResponseInputStream<>(response, new ByteArrayInputStream(new byte[10]));

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        S3FileDto result = s3Service.downloadFile(key);

        assertNotNull(result);
        assertEquals("file.txt", result.getFileName());
        assertEquals("text/plain", result.getContentType());
        assertEquals(10L, result.getContentLength());
        assertNotNull(result.getInputStreamResource());
    }

    @Test
    void testDownloadFile_s3Exception() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        FileException exception = assertThrows(FileException.class,
                () -> s3Service.downloadFile(key));
        assertEquals("Failed to download file", exception.getMessage());
    }

    @Test
    void testDownloadFile_unexpectedException() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        FileException exception = assertThrows(FileException.class,
                () -> s3Service.downloadFile(key));
        assertEquals("Failed to download file", exception.getMessage());
    }
}