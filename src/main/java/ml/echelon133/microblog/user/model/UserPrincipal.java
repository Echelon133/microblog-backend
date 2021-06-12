package ml.echelon133.microblog.user.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public interface UserPrincipal {
    UUID getUuid();
    String getUsername();
    Collection<? extends GrantedAuthority> getAuthorities();
}
