package ml.echelon133.microblog.post.model;

import org.hibernate.validator.constraints.Length;

public class PostDto {

    @Length(min = 1, max = 300, message = "Post length is invalid")
    private String content;

    public PostDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
