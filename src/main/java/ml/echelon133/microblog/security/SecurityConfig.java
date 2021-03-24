package ml.echelon133.microblog.security;

import ml.echelon133.microblog.auth.CustomAuthenticationFilter;
import ml.echelon133.microblog.auth.CustomAuthenticationManager;
import ml.echelon133.microblog.auth.CustomAuthenticationProvider;
import ml.echelon133.microblog.token.ITokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;
    private ITokenService tokenService;

    @Autowired
    public SecurityConfig(@Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
                          ITokenService tokenService) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
    }

    @Bean
    public CustomAuthenticationProvider customAuthProvider() {
        return new CustomAuthenticationProvider(userDetailsService, tokenService);
    }

    @Bean
    public CustomAuthenticationManager customAuthManager() {
        return new CustomAuthenticationManager(customAuthProvider());
    }

    @Bean
    public CustomAuthenticationFilter customAuthFilter() {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter();
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
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/users/me").hasRole("USER")
                .antMatchers(HttpMethod.GET, "/api/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/**").hasRole("USER")
                .antMatchers(HttpMethod.PUT, "/api/**").hasRole("USER")
                .antMatchers(HttpMethod.DELETE, "/api/**").hasRole("USER")
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
