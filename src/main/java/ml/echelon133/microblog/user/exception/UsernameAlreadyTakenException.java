package ml.echelon133.microblog.user.exception;

public class UsernameAlreadyTakenException extends Exception {

    public UsernameAlreadyTakenException() {
    }

    public UsernameAlreadyTakenException(String message) {
        super(message);
    }
}
