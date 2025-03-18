package com.github.filefusion.config;

import com.github.filefusion.common.SecurityProperties;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.service.UserService;
import com.github.filefusion.util.I18n;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final String CLAIM_SCOPE = "scope";

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
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions((HeadersConfigurer.FrameOptionsConfig::sameOrigin)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((request, response, authException) -> {
                            throw authException;
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            throw accessDeniedException;
                        })
                );
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

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter = source -> {
            List<String> permissionIds = source.getClaim(CLAIM_SCOPE);
            return permissionIds.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        };
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            Jws<Claims> jws;
            try {
                jws = Jwts.parser()
                        .verifyWith(securityProperties.getSecret().getPublicKey()).build()
                        .parseSignedClaims(token);
            } catch (Exception e) {
                throw new BadCredentialsException(I18n.get("badCredentials"));
            }
            Map<String, Object> header = new LinkedHashMap<>(jws.getHeader());
            Map<String, Object> payload = new LinkedHashMap<>(jws.getPayload());
            String userId = (String) payload.get(JwtClaimNames.SUB);
            String tokenId = (String) payload.get(JwtClaimNames.JTI);
            userService.verifyToken(userId, tokenId);
            UserInfo user = userService.getById(userId);
            userService.verifyUser(user);
            payload.put(CLAIM_SCOPE, user.getPermissionIds());
            return new Jwt(token, Instant.ofEpochMilli((Long) payload.get(JwtClaimNames.IAT)),
                    null, header, payload);
        };
    }

}
