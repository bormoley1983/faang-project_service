package faang.school.projectservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ResizedMultipartFile implements MultipartFile {
    private final MultipartFile originalFile;
    private final byte[] resizedImageBytes;

    public ResizedMultipartFile(MultipartFile originalFile, byte[] resizedImageBytes) {
        this.originalFile = originalFile;
        this.resizedImageBytes = resizedImageBytes == null
                ? new byte[0]
                : Arrays.copyOf(resizedImageBytes, resizedImageBytes.length);
    }

    @Override
    public String getName() {
        return originalFile.getName();
    }

    @Override
    public String getOriginalFilename() {
        return originalFile.getOriginalFilename();
    }

    @Override
    public String getContentType() {
        return originalFile.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return resizedImageBytes == null || resizedImageBytes.length == 0;
    }

    @Override
    public long getSize() {
        return resizedImageBytes.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Arrays.copyOf(resizedImageBytes, resizedImageBytes.length);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(resizedImageBytes);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("Transfer to file is not supported");
    }
}
