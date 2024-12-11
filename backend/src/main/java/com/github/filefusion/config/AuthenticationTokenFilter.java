package com.github.filefusion.config;

import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.user.model.UserToken;
import com.github.filefusion.util.I18n;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;

/**
 * AuthenticationTokenFilter
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final static AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final UserDetailsService userDetailsService;

    public AuthenticationTokenFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasLength(authorization)) {
            UserToken token;
            try {
                token = UserToken.decoder(authorization);
            } catch (Exception e) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), I18n.get("tokenError"));
                return;
            }
            UserInfo user = (UserInfo) userDetailsService.loadUserByUsername(token.getUserId());
            if (token.getCreatedDate().before(user.getEarliestCredentials())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), I18n.get("tokenExpired"));
                return;
            }
            try {
                user.verifyUser();
            } catch (AccountStatusException e) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
                return;
            }
            SecurityContextHolder.getContext()
                    .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, user.getPassword(), user.getAuthorities()));
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String lookupPath = UrlPathHelper.defaultInstance.getLookupPathForRequest(request);
        return !PATH_MATCHER.match(ApiPrefixConfig.CONTEXT_PATH + "/**", lookupPath);
    }

}
