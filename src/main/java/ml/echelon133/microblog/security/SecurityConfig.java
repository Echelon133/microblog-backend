package ml.echelon133.microblog.security;

import ml.echelon133.microblog.auth.CustomAuthenticationFilter;
import ml.echelon133.microblog.auth.CustomAuthenticationManager;
import ml.echelon133.microblog.auth.CustomAuthenticationProvider;
import ml.echelon133.microblog.token.ITokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private ITokenService tokenService;

    @Autowired
    public SecurityConfig(ITokenService tokenService) {
        this.tokenService = tokenService;
    }

    public RequestMatcher customRequestMatcher() {
        return new AntPathRequestMatcher("**");
    }

    public CustomAuthenticationProvider customAuthProvider() {
        return new CustomAuthenticationProvider(tokenService);
    }

    public CustomAuthenticationManager customAuthManager() {
        return new CustomAuthenticationManager(customAuthProvider());
    }

    public CustomAuthenticationFilter customAuthFilter() {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(customRequestMatcher());
        filter.setAuthenticationManager(customAuthManager());
        filter.setContinueChainBeforeSuccessfulAuthentication(true);
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().and()
                .httpBasic().disable()
                .requestMatcher(customRequestMatcher()).addFilterBefore(customAuthFilter(), BasicAuthenticationFilter.class)
                    .authorizeRequests()
                        .antMatchers(HttpMethod.GET, "/api/notifications").hasRole("USER")
                        .antMatchers(HttpMethod.GET, "/api/notifications/unreadCounter").hasRole("USER")
                        .antMatchers(HttpMethod.GET, "/api/users/me").hasRole("USER")
                        .antMatchers(HttpMethod.GET, "/api/posts/*/like").hasRole("USER")
                        .antMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .anyRequest().hasRole("USER")
                    .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
