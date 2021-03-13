package ml.echelon133.microblog.user;

import java.util.List;

public class NewUserDataInvalidException extends Exception {
    private List<String> messages;

    public NewUserDataInvalidException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }

}
