package faang.school.projectservice.service;

import faang.school.projectservice.exception.ImageResizeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ImageResizer}.
 */
@DisplayName("ImageResizer")
class ImageResizerTest {

    private ImageResizer imageResizer;

    @BeforeEach
    void setUp() {
        imageResizer = new ImageResizer();
    }

    @Test
    @DisplayName("resizes a valid PNG image to target dimensions")
    void resizesValidImage() throws IOException {
        byte[] originalBytes = createPngBytes(200, 200);

        byte[] result = imageResizer.resizeImage(originalBytes, 100, 100);

        assertThat(result).isNotEmpty();
        // Verify the result is a valid JPEG
        BufferedImage decoded = ImageIO.read(new java.io.ByteArrayInputStream(result));
        assertThat(decoded).isNotNull();
        assertThat(decoded.getWidth()).isEqualTo(100);
        assertThat(decoded.getHeight()).isEqualTo(100);
    }

    @Test
    @DisplayName("throws ImageResizeException for null/invalid image data")
    void throwsForInvalidImageData() {
        byte[] invalidBytes = new byte[]{1, 2, 3, 4, 5}; // not a valid image

        assertThatThrownBy(() -> imageResizer.resizeImage(invalidBytes, 100, 100))
                .isInstanceOf(ImageResizeException.class)
                .hasMessageContaining("Unsupported or invalid image data");
    }

    @Test
    @DisplayName("throws ImageResizeException for empty byte array")
    void throwsForEmptyByteArray() {
        byte[] emptyBytes = new byte[0];

        assertThatThrownBy(() -> imageResizer.resizeImage(emptyBytes, 100, 100))
                .isInstanceOf(ImageResizeException.class);
    }

    @Test
    @DisplayName("resizes a wide image to smaller dimensions")
    void resizesWideImage() throws IOException {
        byte[] originalBytes = createPngBytes(400, 200);

        byte[] result = imageResizer.resizeImage(originalBytes, 150, 100);

        assertThat(result).isNotEmpty();
        BufferedImage decoded = ImageIO.read(new java.io.ByteArrayInputStream(result));
        assertThat(decoded).isNotNull();
        // Scalr preserves aspect ratio; verify the result is smaller than original
        assertThat(decoded.getWidth()).isLessThan(400);
        assertThat(decoded.getHeight()).isLessThan(200);
    }

    private byte[] createPngBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
