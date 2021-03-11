package ml.echelon133.microblog.user;

public class UserCreationFailedException extends Exception {

    public UserCreationFailedException() {
    }

    public UserCreationFailedException(String message) {
        super(message);
    }
}
