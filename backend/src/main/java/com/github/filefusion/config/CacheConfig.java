package com.github.filefusion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheNativeManager;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CacheConfig
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
@EnableCaching
public class CacheConfig implements RedissonAutoConfigurationCustomizer {

    private final ObjectMapper objectMapper;

    @Autowired
    public CacheConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void customize(Config configuration) {
        configuration.setCodec(new JsonJacksonCodec(objectMapper));
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        return new RedissonSpringCacheNativeManager(redissonClient);
    }

}
