package ml.echelon133.microblog.post.model;

import org.hibernate.validator.constraints.Length;

public class QuotePostDto {

    @Length(min= 1, max = 300, message = "Quote length is invalid")
    private String content;

    public QuotePostDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
