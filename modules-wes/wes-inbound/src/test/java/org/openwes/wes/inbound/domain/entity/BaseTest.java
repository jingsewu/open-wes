package org.openwes.wes.inbound.domain.entity;

import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.utils.RedisUtils;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

public class BaseTest {
    public void setUp() {
        RedisUtils mockRedisUtils = Mockito.mock(RedisUtils.class);
        Mockito.when(mockRedisUtils.getAndIncrement(anyString(), anyInt())).thenReturn(10L);
        new OrderNoGenerator(mockRedisUtils);
    }
}
