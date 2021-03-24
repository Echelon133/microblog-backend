package ml.echelon133.microblog.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenService implements ITokenService {

    private AccessTokenRepository accessTokenRepository;

    @Autowired
    public TokenService(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public Optional<AccessToken> findByAccessToken(String accessToken) {
        return accessTokenRepository.findById(accessToken);
    }
}
