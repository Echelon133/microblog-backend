package ml.echelon133.microblog.post.exception;

import java.util.UUID;

public class PostDoesntExistException extends Exception {

    public PostDoesntExistException(UUID uuid) {
        super(String.format("Post with UUID %s doesn't exist", uuid.toString()));
    }
}
