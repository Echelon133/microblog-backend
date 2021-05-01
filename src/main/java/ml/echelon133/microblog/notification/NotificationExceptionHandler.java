package ml.echelon133.microblog.notification;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice(assignableTypes = NotificationController.class)
public class NotificationExceptionHandler extends AbstractExceptionHandler {
}
