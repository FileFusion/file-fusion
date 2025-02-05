package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final static String PREFIX = "lock:";
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
        RLock lock = redissonClient.getLock(PREFIX + key);
        tryLock(lock, task);
    }

    public void tryMultiLock(List<String> keyList, Runnable task) {
        if (CollectionUtils.isEmpty(keyList)) {
            return;
        }
        RLock[] locks = keyList.stream().map(k -> PREFIX + k).map(redissonClient::getLock).toArray(RLock[]::new);
        RLock multiLock = redissonClient.getMultiLock(locks);
        tryLock(multiLock, task);
    }

    private void tryLock(RLock lock, Runnable task) {
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTimeout.toMillis(), -1, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw new HttpException(I18n.get("lockAcquisitionFailed"));
            }
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
