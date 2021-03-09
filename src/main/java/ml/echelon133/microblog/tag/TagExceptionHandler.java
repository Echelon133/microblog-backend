package ml.echelon133.microblog.tag;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice(assignableTypes = TagController.class)
public class TagExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(value = TagDoesntExistException.class)
    protected ResponseEntity<ErrorMessage> handleTagDoesntExistException(TagDoesntExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
