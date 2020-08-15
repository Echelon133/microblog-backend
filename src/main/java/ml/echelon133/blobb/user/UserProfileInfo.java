package ml.echelon133.blobb.user;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.UUID;

@QueryResult
public class UserProfileInfo {
    @Convert(value = UuidStringConverter.class)
    private UUID uuid;
    private Long follows;
    private Long followedBy;

    public UUID getUuid() {
        return uuid;
    }

    public Long getFollows() {
        return follows;
    }

    public Long getFollowedBy() {
        return followedBy;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setFollows(Long follows) {
        this.follows = follows;
    }

    public void setFollowedBy(Long followedBy) {
        this.followedBy = followedBy;
    }
}
