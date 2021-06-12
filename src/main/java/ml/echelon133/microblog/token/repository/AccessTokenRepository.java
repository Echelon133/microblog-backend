package ml.echelon133.microblog.token.repository;

import ml.echelon133.microblog.token.model.AccessToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenRepository extends CrudRepository<AccessToken, String> {
    Optional<AccessToken> findByOwnerUsername(String username);
}
