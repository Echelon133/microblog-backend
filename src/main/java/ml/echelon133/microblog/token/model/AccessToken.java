package ml.echelon133.microblog.token.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

// access tokens should expire after 60 minutes
@RedisHash(value="accessToken", timeToLive = 3600)
public class AccessToken implements Serializable {

    @Id
    private String token;
    private UUID ownerUuid;
    private List<String> roles;

    @Indexed
    private String ownerUsername;

    public AccessToken() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
