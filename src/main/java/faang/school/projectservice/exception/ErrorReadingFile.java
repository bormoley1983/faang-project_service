package faang.school.projectservice.exception;

import java.io.IOException;

public class ErrorReadingFile extends RuntimeException {
    public ErrorReadingFile(String message, IOException e) {
        super(message, e);
    }
}
