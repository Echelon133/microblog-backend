package ml.echelon133.blobb.blobb;

import java.util.UUID;

public class BlobbDoesntExistException extends Exception {

    public BlobbDoesntExistException(UUID uuid) {
        super(String.format("Blobb with UUID %s doesn't exist", uuid.toString()));
    }
}
