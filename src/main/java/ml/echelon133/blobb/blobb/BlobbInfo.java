package ml.echelon133.blobb.blobb;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.UUID;

@QueryResult
public class BlobbInfo {
    @Convert(value = UuidStringConverter.class)
    private UUID uuid;
    private Long responses;
    private Long likes;
    private Long reblobbs;

    public BlobbInfo() {}

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

    public Long getReblobbs() {
        return reblobbs;
    }

    public void setReblobbs(Long reblobbs) {
        this.reblobbs = reblobbs;
    }
}
