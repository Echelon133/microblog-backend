package ml.echelon133.blobb.blobb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ml.echelon133.blobb.tag.Tag;
import ml.echelon133.blobb.user.User;
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
public class Blobb {
    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    @Convert(UuidStringConverter.class)
    private UUID uuid;
    private String content;
    private Date creationDate;

    @Relationship(value = "POSTS", direction = Relationship.INCOMING)
    private User author;

    @Relationship(type = "TAGS", direction = Relationship.INCOMING)
    private Set<Tag> tags;

    public Blobb() {
        this.tags = new HashSet<>();
        this.creationDate = new Date();
    }

    public Blobb(User author, String content) {
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
}
