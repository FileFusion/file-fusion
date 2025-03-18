package com.github.filefusion.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * RequestUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class RequestUtil {

    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    );

    public static String getClientIp(HttpServletRequest request) {
        String ip = IP_HEADERS.stream()
                .map(request::getHeader)
                .filter(header -> StringUtils.hasLength(header) && !"unknown".equalsIgnoreCase(header))
                .findFirst()
                .orElse(null);
        if (ip != null && ip.contains(",")) {
            ip = Arrays.stream(ip.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && !"unknown".equalsIgnoreCase(s))
                    .findFirst()
                    .orElse(null);
        }
        return Optional.ofNullable(ip).orElse(request.getRemoteAddr());
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }

}
