package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.RedisAttribute;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DistributedLock
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class DistributedLock {

    private final RedissonClient redissonClient;
    private final Duration waitTimeout;

    @Autowired
    public DistributedLock(RedissonClient redissonClient,
                           @Value("${server.servlet.session.timeout}") Duration timeout) {
        this.redissonClient = redissonClient;
        this.waitTimeout = timeout;
    }

    public void tryLock(String key, Runnable task) {
        if (!StringUtils.hasLength(key)) {
            return;
        }
        RLock lock = redissonClient.getLock(RedisAttribute.LOCK_PREFIX + RedisAttribute.SEPARATOR + key);
        tryLock(lock, task);
    }

    public void tryMultiLock(List<String> keyList, Runnable task) {
        if (CollectionUtils.isEmpty(keyList)) {
            return;
        }
        RLock[] locks = keyList.stream().map(k -> RedisAttribute.LOCK_PREFIX + RedisAttribute.SEPARATOR + k).map(redissonClient::getLock).toArray(RLock[]::new);
        RLock multiLock = redissonClient.getMultiLock(locks);
        tryLock(multiLock, task);
    }

    private void tryLock(RLock lock, Runnable task) {
        final AtomicBoolean isLockAcquired = new AtomicBoolean(false);
        try {
            isLockAcquired.set(lock.tryLock(waitTimeout.toMillis(), TimeUnit.MILLISECONDS));
            if (!isLockAcquired.get()) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("lockAcquisitionFailed"));
            }
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (isLockAcquired.get() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
