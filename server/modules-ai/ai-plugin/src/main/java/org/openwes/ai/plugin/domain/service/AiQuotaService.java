package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiQuotaService {

    static final String KEY_PREFIX = "ai:plugin:draft:quota:";

    private final RedisUtils redisUtils;

    /**
     * Atomic per-user daily counter. {@code getAndIncrement} returns the previous value, so the
     * Nth accepted call sees N-1. A non-positive limit means "unlimited" and is the operator's
     * explicit escape hatch.
     */
    public boolean tryAcquire(String user, int dailyLimit) {
        if (dailyLimit <= 0) {
            return true;
        }
        String key = KEY_PREFIX + user + ":" + LocalDate.now();
        long previous = redisUtils.getAndIncrement(key, 1);
        return previous < dailyLimit;
    }
}
