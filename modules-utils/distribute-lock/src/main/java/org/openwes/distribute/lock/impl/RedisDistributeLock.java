package org.openwes.distribute.lock.impl;

import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.distribute.lock.DistributeLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisDistributeLock implements DistributeLock {

    private final RedisUtils redisUtils;

    @Override
    public boolean acquireLock(String lockKey, long waitTimeInMillis) {
        RLock lock = redisUtils.getLock(lockKey);
        try {
            return lock.tryLock(waitTimeInMillis, 60000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean acquireLock(List<String> lockKeys, long waitTimeInMillis) {
        RLock lock = redisUtils.getLock(lockKeys);
        try {
            return lock.tryLock(waitTimeInMillis, 60000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void releaseLock(String lockKey) {
        RLock lock = redisUtils.getLock(lockKey);
        lock.unlock();
    }

    @Override
    public void releaseLock(List<String> lockKeys) {
        RLock lock = redisUtils.getLock(lockKeys);
        lock.unlock();
    }

    @Override
    public void acquireLockIfThrows(String lockKey, long waitTimeInMillis) {
        boolean acquireLock = acquireLock(lockKey, waitTimeInMillis);
        if (!acquireLock) {
            throw new CommonException("request repeated, please try again");
        }
    }

    @Override
    public void acquireLockIfThrows(String lockKey) {
        acquireLockIfThrows(lockKey, 3000L);
    }

    @Override
    public void acquireLockIfThrows(List<String> lockKeys, long waitTimeInMillis) {
        boolean acquireLock = acquireLock(lockKeys, waitTimeInMillis);
        if (!acquireLock) {
            throw new CommonException("request repeated, please try again");
        }
    }
}
