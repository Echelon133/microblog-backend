package ml.echelon133.microblog.notification.exception;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import ml.echelon133.microblog.notification.controller.NotificationController;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice(assignableTypes = NotificationController.class)
public class NotificationExceptionHandler extends AbstractExceptionHandler {
}
