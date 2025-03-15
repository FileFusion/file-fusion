package com.github.filefusion.config;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheNativeManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CacheConfig
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        return new RedissonSpringCacheNativeManager(redissonClient);
    }

}
