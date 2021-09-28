package ml.echelon133.microblog.user.model;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class UserProfileInfo {

    private Long follows;
    private Long followers;

    public Long getFollows() {
        return follows;
    }

    public Long getFollowers() {
        return followers;
    }

    public void setFollows(Long follows) {
        this.follows = follows;
    }

    public void setFollowers(Long followers) {
        this.followers = followers;
    }
}
