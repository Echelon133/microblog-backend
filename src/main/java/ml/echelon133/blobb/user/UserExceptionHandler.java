package ml.echelon133.blobb.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler extends ResponseEntityExceptionHandler {

    static class ErrorMessage {
        private Date timestamp;
        private List<String> messages;
        private String path;

        ErrorMessage(Date timestamp, String path, String... messages) {
            this.timestamp = timestamp;
            this.path = path;
            this.messages = Arrays.asList(messages);
        }

        ErrorMessage(Date timestamp, String path, List<String> messages) {
            this.timestamp = timestamp;
            this.path = path;
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
    }

    @ExceptionHandler(value = UserDoesntExistException.class)
    protected ResponseEntity<ErrorMessage> handleUserDoesntExistException(UserDoesntExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                                 request.getDescription(false),
                                 ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    protected ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }


}
