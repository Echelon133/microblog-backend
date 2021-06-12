package ml.echelon133.microblog.auth;

import ml.echelon133.microblog.user.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomAuthToken implements Authentication {

    private User user;
    private String accessToken;

    public static class User implements UserPrincipal {
        private UUID uuid;
        private String username;
        private List<GrantedAuthority> authorities;

        public User(UUID uuid, String username, List<String> r) {
            this.uuid = uuid;
            this.username = username;
            this.authorities = r
                    .stream()
                    .map(role -> (GrantedAuthority) () -> role)
                    .collect(Collectors.toList());
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getUsername() {
            return username;
        }

        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }
    }

    public CustomAuthToken(UUID uuid, String username, List<String> r, String accessToken) {
        this.user = new User(uuid, username, r);
        this.accessToken = accessToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}
