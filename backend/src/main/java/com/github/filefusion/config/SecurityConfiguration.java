package com.github.filefusion.config;

import com.github.filefusion.common.SecurityProperties;
import com.github.filefusion.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

/**
 * SecurityConfiguration
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final SecurityProperties securityProperties;
    private final WebServerFactory webServerFactory;
    private final UserService userService;

    @Autowired
    public SecurityConfiguration(SecurityProperties securityProperties,
                                 WebServerFactory webServerFactory,
                                 UserService userService) {
        this.securityProperties = securityProperties;
        this.webServerFactory = webServerFactory;
        this.userService = userService;
    }

    private static String buildFullPath(String path) {
        return UriComponentsBuilder.fromPath(WebConfig.CONTEXT_PATH).path(path).build().toUriString();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        webServerFactory.setSslRedirect(http);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions((HeadersConfigurer.FrameOptionsConfig::sameOrigin)))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AuthenticationTokenFilter(userService), UsernamePasswordAuthenticationFilter.class);
        configureWhitelistAccess(http);
        return http.build();
    }

    private void configureWhitelistAccess(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeHttpRequests -> {
            Arrays.stream(securityProperties.getAllWhitelist())
                    .map(SecurityConfiguration::buildFullPath)
                    .map(AntPathRequestMatcher::new)
                    .forEach(matcher -> authorizeHttpRequests.requestMatchers(matcher).permitAll());
            securityProperties.getWhitelist().entrySet().stream()
                    .flatMap(entry -> Arrays.stream(entry.getValue())
                            .map(path -> new AntPathRequestMatcher(buildFullPath(path), entry.getKey().name()))
                    ).forEach(matcher -> authorizeHttpRequests.requestMatchers(matcher).permitAll());
            authorizeHttpRequests.requestMatchers(new AntPathRequestMatcher(buildFullPath("/**")))
                    .authenticated().anyRequest().permitAll();
        });
    }

}
