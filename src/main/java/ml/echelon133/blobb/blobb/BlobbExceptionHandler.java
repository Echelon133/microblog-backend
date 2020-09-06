package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.exception.AbstractExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice(assignableTypes = BlobbController.class)
public class BlobbExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(value = BlobbDoesntExistException.class)
    protected ResponseEntity<ErrorMessage> handleBlobbDoesntExistException(BlobbDoesntExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = InvalidBlobbContentException.class)
    protected ResponseEntity<ErrorMessage> handleInvalidBlobbContentException(InvalidBlobbContentException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = UserCannotDeleteBlobbException.class)
    protected ResponseEntity<ErrorMessage> handleUserCannotDeleteBlobbException(UserCannotDeleteBlobbException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.FORBIDDEN,
                        ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }
}
