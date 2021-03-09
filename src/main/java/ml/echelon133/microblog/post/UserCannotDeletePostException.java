package ml.echelon133.microblog.post;

import ml.echelon133.microblog.user.User;

import java.util.UUID;

public class UserCannotDeletePostException extends Exception {

    public UserCannotDeletePostException(User user, UUID postUuid) {
        super(String.format("User %s cannot delete post with %s uuid", user.getUsername(), postUuid));
    }
}
