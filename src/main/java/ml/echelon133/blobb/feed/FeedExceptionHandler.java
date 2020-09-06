package ml.echelon133.blobb.feed;

import ml.echelon133.blobb.exception.AbstractExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice(assignableTypes = FeedController.class)
public class FeedExceptionHandler extends AbstractExceptionHandler {
}
