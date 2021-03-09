package ml.echelon133.microblog.feed;

import ml.echelon133.microblog.exception.AbstractExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice(assignableTypes = FeedController.class)
public class FeedExceptionHandler extends AbstractExceptionHandler {
}
