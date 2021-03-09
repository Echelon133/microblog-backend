package ml.echelon133.microblog.post;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice(assignableTypes = PostController.class)
public class PostExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(value = PostDoesntExistException.class)
    protected ResponseEntity<ErrorMessage> handlePostDoesntExistException(PostDoesntExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = InvalidPostContentException.class)
    protected ResponseEntity<ErrorMessage> handleInvalidPostContentException(InvalidPostContentException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = UserCannotDeletePostException.class)
    protected ResponseEntity<ErrorMessage> handleUserCannotDeletePostException(UserCannotDeletePostException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.FORBIDDEN,
                        ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }
}
