package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.RedisAttribute;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@Component
public class DistributedLock {

    private final RedissonClient redissonClient;

    @Autowired
    public DistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void tryLock(RedisAttribute.LockType lockType, String key, Runnable task, Duration lockTimeout) {
        if (lockType == null || !StringUtils.hasLength(key) || lockTimeout == null || lockTimeout.isNegative()) {
            return;
        }
        RLock lock = redissonClient.getLock(RedisAttribute.LOCK_PREFIX + RedisAttribute.SEPARATOR + lockType + RedisAttribute.SEPARATOR + key);
        tryLock(lock, task, lockTimeout);
    }

    public void tryMultiLock(RedisAttribute.LockType lockType, List<String> keyList, Runnable task, Duration lockTimeout) {
        if (lockType == null || CollectionUtils.isEmpty(keyList) || lockTimeout == null || lockTimeout.isNegative()) {
            return;
        }
        RLock[] locks = keyList.stream()
                .map(k -> RedisAttribute.LOCK_PREFIX + RedisAttribute.SEPARATOR + lockType + RedisAttribute.SEPARATOR + k)
                .map(redissonClient::getLock).toArray(RLock[]::new);
        RLock multiLock = redissonClient.getMultiLock(locks);
        tryLock(multiLock, task, lockTimeout);
    }

    private void tryLock(RLock lock, Runnable task, Duration lockTimeout) {
        boolean isLockAcquired = false;
        try {
            isLockAcquired = lock.tryLock(lockTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!isLockAcquired) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("lockAcquisitionFailed"));
            }
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (isLockAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
