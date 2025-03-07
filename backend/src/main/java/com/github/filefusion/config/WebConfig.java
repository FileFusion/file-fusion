package com.github.filefusion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * ApiPrefixConfig
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String CONTEXT_PATH = "/api";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(CONTEXT_PATH, c -> c.isAnnotationPresent(Controller.class))
                .addPathPrefix(CONTEXT_PATH, c -> c.isAnnotationPresent(RestController.class));
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(converter -> converter instanceof StringHttpMessageConverter);
    }

}
