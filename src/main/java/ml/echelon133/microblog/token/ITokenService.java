package ml.echelon133.microblog.token;

import java.util.Optional;

public interface ITokenService {
    Optional<AccessToken> findByAccessToken(String accessToken);
}
