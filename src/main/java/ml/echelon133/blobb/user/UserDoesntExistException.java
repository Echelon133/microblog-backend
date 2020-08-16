package ml.echelon133.blobb.user;

import java.util.UUID;

public class UserDoesntExistException extends Exception {
    public UserDoesntExistException(UUID uuid) {
        super(String.format("User with UUID %s doesn't exist", uuid.toString()));
    }
}
