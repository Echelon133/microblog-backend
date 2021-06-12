package ml.echelon133.microblog.user.model;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.UUID;

@QueryResult
public class UserProfileInfo {
    @Convert(value = UuidStringConverter.class)
    private UUID uuid;
    private Long follows;
    private Long followers;

    public UUID getUuid() {
        return uuid;
    }

    public Long getFollows() {
        return follows;
    }

    public Long getFollowers() {
        return followers;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setFollows(Long follows) {
        this.follows = follows;
    }

    public void setFollowers(Long followers) {
        this.followers = followers;
    }
}
