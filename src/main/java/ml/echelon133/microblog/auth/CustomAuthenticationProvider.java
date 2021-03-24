package ml.echelon133.microblog.auth;

import ml.echelon133.microblog.token.AccessToken;
import ml.echelon133.microblog.token.ITokenService;
import ml.echelon133.microblog.user.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;
    private ITokenService tokenService;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, ITokenService tokenService) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TemporaryToken token = (TemporaryToken) authentication;
        String accessToken = (String)token.getPrincipal();

        if (accessToken == null) {
            throw new BadCredentialsException("Invalid token");
        }

        // retrieve access token from the token store
        // access token also holds the username of the user that owns the token
        Optional<AccessToken> retrievedToken = tokenService.findByAccessToken(accessToken);

        // token not found in the token store - either expired or never existed
        if (retrievedToken.isEmpty()) {
            throw new BadCredentialsException("Invalid token");
        }

        String username = retrievedToken.get().getOwnerUsername();
        User retrievedUser = (User)userDetailsService.loadUserByUsername(username);

        if (retrievedUser == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomAuthToken(retrievedUser, accessToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthToken.class.isAssignableFrom(authentication);
    }
}
