package ml.echelon133.microblog.post.exception;

public class InvalidPostContentException extends Exception {

    public InvalidPostContentException() {
        super("Content of post rejected");
    }

    public InvalidPostContentException(String message) {
        super(message);
    }
}
