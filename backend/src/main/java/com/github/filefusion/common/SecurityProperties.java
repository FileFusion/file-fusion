package com.github.filefusion.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SecurityProperties
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Component
@ConfigurationProperties("security")
public class SecurityProperties {

    private String[] allWhitelist;
    private Map<HttpMethod, String[]> whitelist;

}
