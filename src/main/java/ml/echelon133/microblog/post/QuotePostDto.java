package ml.echelon133.microblog.post;

import org.hibernate.validator.constraints.Length;

public class QuotePostDto {

    @Length(max = 300, message = "Quote length is invalid")
    private String content;

    public QuotePostDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
