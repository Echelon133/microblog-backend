package ml.echelon133.microblog.report;

import java.util.List;

public class InvalidReportDataException extends Exception {
    private List<String> messages;

    public InvalidReportDataException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
