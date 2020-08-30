package ml.echelon133.blobb.blobb;

import org.hibernate.validator.constraints.Length;

public class BlobbDto {

    @Length(min = 1, max = 300, message = "Blobb length is invalid")
    private String content;

    public BlobbDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
