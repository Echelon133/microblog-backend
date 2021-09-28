package ml.echelon133.microblog.post.model;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class PostInfo {

    private Long responses;
    private Long likes;
    private Long quotes;

    public PostInfo() {}

    public Long getResponses() {
        return responses;
    }

    public void setResponses(Long responses) {
        this.responses = responses;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getQuotes() {
        return quotes;
    }

    public void setQuotes(Long quotes) {
        this.quotes = quotes;
    }
}
