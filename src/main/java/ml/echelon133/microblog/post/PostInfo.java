package ml.echelon133.microblog.post;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.UUID;

@QueryResult
public class PostInfo {
    @Convert(value = UuidStringConverter.class)
    private UUID uuid;
    private Long responses;
    private Long likes;
    private Long quotes;

    public PostInfo() {}

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

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
