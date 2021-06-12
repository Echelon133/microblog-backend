package ml.echelon133.microblog.user.exception;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import ml.echelon133.microblog.user.controller.UserController;
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

    @ExceptionHandler(value = InvalidUserDetailsFieldException.class)
    protected ResponseEntity<ErrorMessage> handleInvalidUserDetailsFieldException(InvalidUserDetailsFieldException ex,
                                                                                  WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessages()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = NewUserDataInvalidException.class)
    protected ResponseEntity<ErrorMessage> handleNewUserDataInvalidException(NewUserDataInvalidException ex,
                                                                                  WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessages()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = UsernameAlreadyTakenException.class)
    protected ResponseEntity<ErrorMessage> handleUsernameAlreadyTakenException(UsernameAlreadyTakenException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = UserCreationFailedException.class)
    protected ResponseEntity<ErrorMessage> handleUserCreationFailedException(UserCreationFailedException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
