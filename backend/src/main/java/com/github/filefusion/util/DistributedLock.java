package com.github.filefusion.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${server.servlet.session.timeout}")
    private Duration timeout;

    @Autowired
    public DistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void tryLock(String key, Runnable task) {
        RLock lock = redissonClient.getLock(key);
        tryLock(lock, task);
    }

    public void tryMultiLock(List<String> keyList, Runnable task) {
        RLock[] locks = new RLock[keyList.size()];
        for (int i = 0; i < keyList.size(); i++) {
            locks[i] = redissonClient.getLock(keyList.get(i));
        }
        RLock lock = redissonClient.getMultiLock(locks);
        tryLock(lock, task);
    }

    private void tryLock(RLock lock, Runnable task) {
        try {
            boolean isLocked = lock.tryLock(timeout.getSeconds(), timeout.getSeconds(), TimeUnit.SECONDS);
            if (isLocked) {
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
