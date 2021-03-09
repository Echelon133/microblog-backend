package ml.echelon133.microblog.tag;

import java.util.UUID;

public class TagDoesntExistException extends Exception {

    public TagDoesntExistException(UUID uuid) {
        super(String.format("Tag with UUID %s doesn't exist", uuid.toString()));
    }

    public TagDoesntExistException(String name) {
        super(String.format("Tag #%s doesn't exist", name));
    }

}
