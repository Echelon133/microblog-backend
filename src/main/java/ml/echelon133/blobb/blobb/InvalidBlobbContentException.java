package ml.echelon133.blobb.blobb;

public class InvalidBlobbContentException extends Exception {

    public InvalidBlobbContentException() {
        super("Content of blobb rejected");
    }

    public InvalidBlobbContentException(String message) {
        super(message);
    }
}
