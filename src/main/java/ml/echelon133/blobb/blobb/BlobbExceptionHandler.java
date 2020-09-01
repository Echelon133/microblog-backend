package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.exception.AbstractExceptionHandler;
import ml.echelon133.blobb.user.UserController;
import ml.echelon133.blobb.user.UserDoesntExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice(assignableTypes = BlobbController.class)
public class BlobbExceptionHandler {

    @ExceptionHandler(value = BlobbDoesntExistException.class)
    protected ResponseEntity<AbstractExceptionHandler.ErrorMessage> handleBlobbDoesntExistException(BlobbDoesntExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new AbstractExceptionHandler.ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    protected ResponseEntity<AbstractExceptionHandler.ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(
                new AbstractExceptionHandler.ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = InvalidBlobbContentException.class)
    protected ResponseEntity<AbstractExceptionHandler.ErrorMessage> handleInvalidBlobbContentException(InvalidBlobbContentException ex, WebRequest request) {
        return new ResponseEntity<>(
                new AbstractExceptionHandler.ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = UserCannotDeleteBlobbException.class)
    protected ResponseEntity<AbstractExceptionHandler.ErrorMessage> handleUserCannotDeleteBlobbException(UserCannotDeleteBlobbException ex, WebRequest request) {
        return new ResponseEntity<>(
                new AbstractExceptionHandler.ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.FORBIDDEN,
                        ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }
}
