package com.github.filefusion.config;

import com.github.filefusion.common.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Map;

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
    private final UserDetailsService userDetailsService;
    private final WebServerFactory webServerFactory;

    @Autowired
    public SecurityConfiguration(SecurityProperties securityProperties,
                                 UserDetailsService userDetailsService,
                                 WebServerFactory webServerFactory) {
        this.securityProperties = securityProperties;
        this.userDetailsService = userDetailsService;
        this.webServerFactory = webServerFactory;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        webServerFactory.setSslRedirect(http);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions((HeadersConfigurer.FrameOptionsConfig::sameOrigin)))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AuthenticationTokenFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(authorizeHttpRequests -> {
            String[] allWhitelist = securityProperties.getAllWhitelist();
            AntPathRequestMatcher[] allWhitelistRequestMatchers = new AntPathRequestMatcher[allWhitelist.length];
            for (int i = 0; i < allWhitelist.length; i++) {
                allWhitelistRequestMatchers[i] = new AntPathRequestMatcher(ApiPrefixConfig.CONTEXT_PATH + allWhitelist[i]);
            }
            authorizeHttpRequests.requestMatchers(allWhitelistRequestMatchers).permitAll();
            Map<HttpMethod, String[]> whitelist = securityProperties.getWhitelist();
            for (HttpMethod method : whitelist.keySet()) {
                String[] wl = whitelist.get(method);
                AntPathRequestMatcher[] wlRequestMatchers = new AntPathRequestMatcher[wl.length];
                for (int i = 0; i < wl.length; i++) {
                    wlRequestMatchers[i] = new AntPathRequestMatcher(ApiPrefixConfig.CONTEXT_PATH + wl[i], method.name());
                }
                authorizeHttpRequests.requestMatchers(wlRequestMatchers).permitAll();
            }
            authorizeHttpRequests.requestMatchers(new AntPathRequestMatcher(ApiPrefixConfig.CONTEXT_PATH + "/**")).authenticated()
                    .anyRequest().permitAll();
        });
        return http.build();
    }

}
