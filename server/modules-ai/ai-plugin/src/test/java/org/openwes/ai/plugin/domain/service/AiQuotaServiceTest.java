package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.utils.RedisUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiQuotaServiceTest {

    private final RedisUtils redisUtils = mock(RedisUtils.class);
    private final AiQuotaService quotaService = new AiQuotaService(redisUtils);

    @Test
    void zeroLimit_disablesTheQuotaWithoutTouchingRedis() {
        assertTrue(quotaService.tryAcquire("alice", 0));

        verify(redisUtils, never()).getAndIncrement(anyString(), eq(1));
    }

    @Test
    void allowsCallsBelowTheDailyLimit_andDeniesTheNextOne() {
        when(redisUtils.getAndIncrement(anyString(), eq(1))).thenReturn(0L, 1L, 2L);

        assertTrue(quotaService.tryAcquire("alice", 2));
        assertTrue(quotaService.tryAcquire("alice", 2));
        assertFalse(quotaService.tryAcquire("alice", 2));
    }

    @Test
    void key_isScopedToUserAndCalendarDay() {
        when(redisUtils.getAndIncrement(anyString(), eq(1))).thenReturn(0L);

        quotaService.tryAcquire("alice", 5);

        verify(redisUtils).getAndIncrement(
                org.mockito.ArgumentMatchers.argThat(key ->
                        key.contains("alice") && key.contains(LocalDate.now().toString())), eq(1));
    }
}
