package ml.echelon133.microblog.post;

import ml.echelon133.microblog.tag.Tag;
import ml.echelon133.microblog.user.User;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NodeEntity
public class Post {
    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    @Convert(UuidStringConverter.class)
    private UUID uuid;
    private String content;
    private Date creationDate;
    private boolean deleted;

    @Relationship(value = "POSTS", direction = Relationship.INCOMING)
    private User author;

    @Relationship(type = "TAGS", direction = Relationship.INCOMING)
    private Set<Tag> tags;

    public Post() {
        this.tags = new HashSet<>();
        this.creationDate = new Date();
    }

    public Post(User author, String content) {
        this();
        this.author = author;
        this.content = content;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getContent() {
        return content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public User getAuthor() {
        return author;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }
}
