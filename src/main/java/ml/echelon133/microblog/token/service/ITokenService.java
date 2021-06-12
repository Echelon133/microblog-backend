package ml.echelon133.microblog.token.service;

import ml.echelon133.microblog.token.model.AccessToken;

import java.util.Optional;

public interface ITokenService {
    Optional<AccessToken> findByAccessToken(String accessToken);
}
