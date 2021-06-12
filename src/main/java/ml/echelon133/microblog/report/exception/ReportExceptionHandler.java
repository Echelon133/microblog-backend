package ml.echelon133.microblog.report.exception;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import ml.echelon133.microblog.report.controller.ReportController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice(assignableTypes = ReportController.class)
public class ReportExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler(value = InvalidReportDataException.class)
    protected ResponseEntity<ErrorMessage> handleInvalidReportDataException(InvalidReportDataException ex,
                                                                                  WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.BAD_REQUEST,
                        ex.getMessages()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = ResourceDoesNotExistException.class)
    protected ResponseEntity<ErrorMessage> handleResourceDoesNotExistException(ResourceDoesNotExistException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorMessage(new Date(),
                        request.getDescription(false),
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }
}
