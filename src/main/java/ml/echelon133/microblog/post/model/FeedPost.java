package ml.echelon133.microblog.post.model;

import ml.echelon133.microblog.user.model.User;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Date;
import java.util.UUID;

@QueryResult
public class FeedPost {
    @Convert(value = UuidStringConverter.class)
    private UUID uuid;
    private String content;
    private Date date;
    private User author;
    @Convert(value = UuidStringConverter.class)
    private UUID quotes;
    @Convert(value = UuidStringConverter.class)
    private UUID respondsTo;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public UUID getQuotes() {
        return quotes;
    }

    public void setQuotes(UUID quotes) {
        this.quotes = quotes;
    }

    public UUID getRespondsTo() {
        return respondsTo;
    }

    public void setRespondsTo(UUID respondsTo) {
        this.respondsTo = respondsTo;
    }
}
