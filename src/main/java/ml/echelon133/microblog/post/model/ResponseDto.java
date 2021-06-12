package ml.echelon133.microblog.post.model;

import org.hibernate.validator.constraints.Length;

public class ResponseDto {

    @Length(min = 1, max = 300, message = "Response length is invalid")
    private String content;

    public ResponseDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
