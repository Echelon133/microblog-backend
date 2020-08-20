package ml.echelon133.blobb.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class AbstractExceptionHandler extends ResponseEntityExceptionHandler {

    public static class ErrorMessage {
        private Date timestamp;
        private List<String> messages;
        private String status;
        private String path;

        public ErrorMessage(Date timestamp, String path, HttpStatus status, String... messages) {
            this.timestamp = timestamp;
            this.path = path;
            this.status = "" + status.value();
            this.messages = Arrays.asList(messages);
        }

        public ErrorMessage(Date timestamp, String path, HttpStatus status, List<String> messages) {
            this.timestamp = timestamp;
            this.path = path;
            this.status = "" + status.value();
            this.messages = messages;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public List<String> getMessages() {
            return messages;
        }

        public String getPath() {
            return path;
        }

        public String getStatus() {
            return status;
        }
    }
}
