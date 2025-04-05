package com.github.filefusion.util;

import com.github.filefusion.constant.RedisAttribute;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DistributedLock
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Slf4j
@Component
public class DistributedLock {

    private final RedissonClient redissonClient;

    @Autowired
    public DistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void tryLock(RedisAttribute.LockType lockType, String key, Runnable task, Duration waitLockTimeout) {
        if (lockType == null || !StringUtils.hasLength(key)) {
            return;
        }
        RLock lock = redissonClient.getLock(RedisAttribute.LOCK_PREFIX + lockType + RedisAttribute.SEPARATOR + key);
        tryLock(lock, task, waitLockTimeout);
    }

    public void tryMultiLock(RedisAttribute.LockType lockType, List<String> keyList, Runnable task, Duration waitLockTimeout) {
        if (lockType == null || CollectionUtils.isEmpty(keyList)) {
            return;
        }
        RLock[] locks = keyList.stream()
                .map(k -> RedisAttribute.LOCK_PREFIX + lockType + RedisAttribute.SEPARATOR + k)
                .map(redissonClient::getLock).toArray(RLock[]::new);
        RLock multiLock = redissonClient.getMultiLock(locks);
        tryLock(multiLock, task, waitLockTimeout);
    }

    private void tryLock(RLock lock, Runnable task, Duration waitLockTimeout) {
        boolean isLockAcquired = false;
        try {
            isLockAcquired = waitLockTimeout == null ? lock.tryLock() : lock.tryLock(waitLockTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!isLockAcquired) {
                return;
            }
            task.run();
        } catch (InterruptedException e) {
            log.error("Error executing command", e);
            Thread.currentThread().interrupt();
        } finally {
            if (isLockAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
