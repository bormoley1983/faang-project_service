package faang.school.projectservice.validator;

import faang.school.projectservice.exception.EmptyFileException;
import faang.school.projectservice.exception.InvalidFileFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FileValidator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileValidator")
class FileValidatorTest {

    private static final long MAX_SIZE = 5_000_000L; // ~5MB

    @Mock
    private MultipartFile file;

    private FileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileValidator(MAX_SIZE);
    }

    @Test
    @DisplayName("throws EmptyFileException when file is null")
    void throwsWhenFileNull() {
        assertThatThrownBy(() -> validator.validateFile(null))
                .isInstanceOf(EmptyFileException.class)
                .hasMessageContaining("empty or null");
    }

    @Test
    @DisplayName("throws EmptyFileException when file is empty")
    void throwsWhenFileEmpty() {
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> validator.validateFile(file))
                .isInstanceOf(EmptyFileException.class)
                .hasMessageContaining("empty or null");
    }

    @Test
    @DisplayName("throws InvalidFileFormatException when content type is not JPEG")
    void throwsWhenNotJpeg() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");

        assertThatThrownBy(() -> validator.validateFile(file))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("Only JPEG");
    }

    @Test
    @DisplayName("throws InvalidFileFormatException when content type is null")
    void throwsWhenContentTypeNull() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);

        assertThatThrownBy(() -> validator.validateFile(file))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("Only JPEG");
    }

    @Test
    @DisplayName("throws MaxUploadSizeExceededException when file exceeds max size")
    void throwsWhenSizeExceeded() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(MAX_SIZE + 1);

        assertThatThrownBy(() -> validator.validateFile(file))
                .isInstanceOf(MaxUploadSizeExceededException.class);
    }

    @Test
    @DisplayName("passes when file is valid JPEG within size limit")
    void passesWhenValid() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(100_000L);

        assertThatCode(() -> validator.validateFile(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("passes when content type is uppercase JPEG")
    void passesWhenUppercaseJpeg() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("IMAGE/JPEG");
        when(file.getSize()).thenReturn(100_000L);

        assertThatCode(() -> validator.validateFile(file)).doesNotThrowAnyException();
    }
}
