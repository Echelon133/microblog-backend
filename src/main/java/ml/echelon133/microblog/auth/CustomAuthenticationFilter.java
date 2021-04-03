package ml.echelon133.microblog.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final String AUTH_HEADER_NAME = "Authorization";
    private final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    public CustomAuthenticationFilter(RequestMatcher requestMatcher) {
        super(requestMatcher);
    }

    private boolean continueChainBeforeSuccessfulAuthentication = false;

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            String header = request.getHeader(AUTH_HEADER_NAME);
            Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
            try {
                boolean requestMatches = super.requiresAuthentication(request, response);
                boolean headerCorrect = header != null && header.startsWith("Bearer");
                boolean cookieIsPresent = cookie != null;
                if ((headerCorrect || cookieIsPresent) && requestMatches) {
                    return true;
                }
            } catch (NullPointerException ignore) {}
        }
        return false;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String header = request.getHeader(AUTH_HEADER_NAME);
        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);

        String accessToken = "";

        // if both the cookie and the 'Authorization' header are set
        // cookies have precedence
        if (cookie != null) {
            accessToken = cookie.getValue();
        } else {
            accessToken = header.substring(7); // skip to the actual token
        }

        TemporaryToken tempToken = new TemporaryToken(accessToken);
        return getAuthenticationManager().authenticate(tempToken);
    }

    @Override
    public void setContinueChainBeforeSuccessfulAuthentication(boolean continueChainBeforeSuccessfulAuthentication) {
        this.continueChainBeforeSuccessfulAuthentication = continueChainBeforeSuccessfulAuthentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (this.continueChainBeforeSuccessfulAuthentication) {
            chain.doFilter(request, response);
        }
    }
}
