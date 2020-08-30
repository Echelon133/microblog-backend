package ml.echelon133.blobb.blobb;

import org.hibernate.validator.constraints.Length;

public class ReblobbDto {

    @Length(max = 300, message = "Blobb length is invalid")
    private String content;

    public ReblobbDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
