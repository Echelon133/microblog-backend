package ml.echelon133.microblog.notification.model;

import ml.echelon133.microblog.post.model.Post;
import ml.echelon133.microblog.user.model.User;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

import java.util.Date;
import java.util.UUID;

public class Notification {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    @Convert(UuidStringConverter.class)
    private UUID uuid;
    private boolean read;
    private String type;
    private Date creationDate;

    @StartNode
    private Post post;

    @EndNode
    private User user;

    public Notification(String type, Post post, User user) {
        this.read = false;
        this.type = type;
        this.creationDate = new Date();
        this.post = post;
        this.user = user;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getType() {
        return type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Post getPost() {
        return post;
    }

    public User getUser() {
        return user;
    }
}
