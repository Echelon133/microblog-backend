package ml.echelon133.blobb.user;

import ml.echelon133.blobb.exception.AbstractExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;


@ControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(value = UserDoesntExistException.class)
    protected ResponseEntity<ErrorMessage> handleUserDoesntExistException(UserDoesntExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                                 request.getDescription(false),
                                 HttpStatus.NOT_FOUND,
                                 ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    protected ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }


}
