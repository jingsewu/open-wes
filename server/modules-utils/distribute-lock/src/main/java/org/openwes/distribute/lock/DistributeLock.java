package org.openwes.distribute.lock;

import java.util.List;

public interface DistributeLock {
    boolean acquireLock(String lockKey, long waitTimeInMillis);

    boolean acquireLock(List<String> lockKeys, long waitTimeInMillis);

    void releaseLock(String lockKey);

    void releaseLock(List<String> lockKeys);

    void acquireLockIfThrows(String lockKey);

    void acquireLockIfThrows(String lockKey, long waitTimeInMillis);

    void acquireLockIfThrows(List<String> lockKeys, long waitTimeInMillis);
}
