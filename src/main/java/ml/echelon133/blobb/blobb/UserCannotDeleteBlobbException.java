package ml.echelon133.blobb.blobb;

import ml.echelon133.blobb.user.User;

import java.util.UUID;

public class UserCannotDeleteBlobbException extends Exception {

    public UserCannotDeleteBlobbException(User user, UUID blobbUuid) {
        super(String.format("User %s cannot delete blobb with %s uuid", user.getUsername(), blobbUuid));
    }
}
