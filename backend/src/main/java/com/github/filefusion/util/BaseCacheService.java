package com.github.filefusion.util;

import com.github.filefusion.common.BaseEntity;
import com.github.filefusion.constant.RedisAttribute;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * BaseCacheService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Slf4j
public abstract class BaseCacheService<T extends BaseEntity> {

    private static final Duration CACHE_EXPIRATION = Duration.ofHours(2);
    private static final Duration NULL_CACHE_EXPIRATION = Duration.ofMinutes(2);

    protected final RedissonClient redissonClient;
    protected final DistributedLock distributedLock;

    protected BaseCacheService(RedissonClient redissonClient,
                               DistributedLock distributedLock) {
        this.redissonClient = redissonClient;
        this.distributedLock = distributedLock;
    }

    protected abstract Class<T> getEntityClass();

    protected abstract String getCacheKeyPrefix();

    protected abstract T queryFromDb(String id) throws EntityNotFoundException;

    public T getByIdFromCache(String id) {
        String cacheKey = buildCacheKey(id);
        RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
        Object cached = bucket.get();
        if (cached != null) {
            return handleCachedValue(cached, cacheKey);
        }
        AtomicReference<T> result = new AtomicReference<>(null);
        distributedLock.tryLock(RedisAttribute.LockType.cache, cacheKey, () -> {
            Object rechecked = bucket.get();
            if (rechecked != null) {
                result.set(handleCachedValue(rechecked, cacheKey));
                return;
            }
            try {
                T entity = queryFromDb(id);
                bucket.set(entity, CACHE_EXPIRATION);
                result.set(entity);
            } catch (EntityNotFoundException e) {
                bucket.set(NullMarker.INSTANCE, NULL_CACHE_EXPIRATION);
                result.set(null);
            }
        }, null);
        return result.get();
    }

    public void deleteCache(String id) {
        String cacheKey = buildCacheKey(id);
        try {
            redissonClient.getBucket(cacheKey).delete();
        } catch (Exception e) {
            log.error("Error deleting cache", e);
        }
    }

    private String buildCacheKey(String id) {
        return RedisAttribute.CACHE_PREFIX + getCacheKeyPrefix() + RedisAttribute.SEPARATOR + id;
    }

    private T handleCachedValue(Object cached, String cacheKey) {
        if (cached instanceof NullMarker) {
            return null;
        }
        if (getEntityClass().isInstance(cached)) {
            return getEntityClass().cast(cached);
        }
        redissonClient.getBucket(cacheKey).delete();
        return null;
    }

    private static final class NullMarker implements Serializable {
        private static final NullMarker INSTANCE = new NullMarker();
    }

}
