package ml.echelon133.microblog.feed.exception;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import ml.echelon133.microblog.feed.controller.FeedController;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice(assignableTypes = FeedController.class)
public class FeedExceptionHandler extends AbstractExceptionHandler {
}
