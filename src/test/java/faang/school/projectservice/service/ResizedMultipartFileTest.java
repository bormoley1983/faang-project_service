package faang.school.projectservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ResizedMultipartFile}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResizedMultipartFile")
class ResizedMultipartFileTest {

    @Mock
    private MultipartFile originalFile;

    private byte[] resizedBytes;

    @BeforeEach
    void setUp() {
        resizedBytes = new byte[]{1, 2, 3, 4, 5};
    }

    @Test
    @DisplayName("delegates getName to original file")
    void delegatesGetName() {
        when(originalFile.getName()).thenReturn("fileField");
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        assertThat(resized.getName()).isEqualTo("fileField");
    }

    @Test
    @DisplayName("delegates getOriginalFilename to original file")
    void delegatesGetOriginalFilename() {
        when(originalFile.getOriginalFilename()).thenReturn("photo.jpg");
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        assertThat(resized.getOriginalFilename()).isEqualTo("photo.jpg");
    }

    @Test
    @DisplayName("delegates getContentType to original file")
    void delegatesGetContentType() {
        when(originalFile.getContentType()).thenReturn("image/jpeg");
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        assertThat(resized.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("isEmpty returns false when resized bytes are present")
    void isEmptyFalseWhenBytesPresent() {
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        assertThat(resized.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("isEmpty returns true when resized bytes are null")
    void isEmptyTrueWhenBytesNull() {
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, null);

        assertThat(resized.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("isEmpty returns true when resized bytes are empty array")
    void isEmptyTrueWhenBytesEmpty() {
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, new byte[0]);

        assertThat(resized.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("getSize returns length of resized bytes")
    void getSizeReturnsLength() {
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        assertThat(resized.getSize()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getBytes returns the resized bytes")
    void getBytesReturnsResizedBytes() throws IOException {
        byte[] expected = resizedBytes.clone();
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);
        resizedBytes[0] = 99;

        byte[] returned = resized.getBytes();
        returned[1] = 99;

        assertThat(resized.getBytes()).containsExactly(expected);
        assertThat(resized.getBytes()).isNotSameAs(resizedBytes);
    }

    @Test
    @DisplayName("getInputStream returns stream over resized bytes")
    void getInputStreamReturnsStream() throws IOException {
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        try (InputStream is = resized.getInputStream()) {
            byte[] read = is.readAllBytes();
            assertThat(read).isEqualTo(resizedBytes);
        }
    }

    @Test
    @DisplayName("transferTo throws UnsupportedOperationException")
    void transferToThrowsUnsupported() {
        ResizedMultipartFile resized = new ResizedMultipartFile(originalFile, resizedBytes);

        assertThatThrownBy(() -> resized.transferTo(java.io.File.createTempFile("test", ".tmp")))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not supported");
    }
}
