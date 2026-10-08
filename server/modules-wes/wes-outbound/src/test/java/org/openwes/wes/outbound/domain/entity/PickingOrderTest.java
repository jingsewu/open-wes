package org.openwes.wes.outbound.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.id.Snowflake;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.plugin.api.dto.event.LifeCycleStatusChangeEvent;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickingOrderTest {

    @BeforeEach
    void setUp() {
        // SnowflakeUtils and OrderNoGenerator hold their collaborators in static fields, populated by
        // Spring in production; unit tests wire them the same way OutboundPlanOrderTest does.
        new SnowflakeUtils(new Snowflake(1, 1));
        RedisUtils mockRedisUtils = Mockito.mock(RedisUtils.class);
        Mockito.when(mockRedisUtils.getAndIncrement(anyString(), anyInt())).thenReturn(10L);
        new OrderNoGenerator(mockRedisUtils);
    }

    private List<LifeCycleStatusChangeEvent> lifecycleEvents(PickingOrder order) {
        return order.getSynchronousDomainEvents().stream()
                .filter(LifeCycleStatusChangeEvent.class::isInstance)
                .map(LifeCycleStatusChangeEvent.class::cast)
                .toList();
    }

    @Test
    void create_emitsLifecycleEventWithEntityNameAndNo() {
        PickingOrder order = PickingOrder.create(1, false, "WH", 1L, "W1", List.of(), false);

        List<LifeCycleStatusChangeEvent> events = lifecycleEvents(order);
        assertEquals(1, events.size());
        assertEquals("PickingOrder", events.get(0).getEntityName());
        assertEquals(PickingOrderStatusEnum.NEW.name(), events.get(0).getNewStatus());
        assertEquals(order.getPickingOrderNo(), events.get(0).getEntityNo());
    }

    @Test
    void dispatch_emitsLifecycleEventWithEntityNameAndNo() {
        PickingOrder order = PickingOrder.create(1, false, "WH", 1L, "W1", List.of(), false);
        // create() leaves pickingOrderStatus null; the PO defaults it to NEW on save, but the
        // in-memory entity must carry it explicitly before lifecycle methods accept the transition.
        order.setPickingOrderStatus(PickingOrderStatusEnum.NEW);
        order.clearEvents();

        order.dispatch(java.util.Map.of(1L, "SLOT-1"));

        List<LifeCycleStatusChangeEvent> events = lifecycleEvents(order);
        assertEquals(1, events.size());
        assertEquals("PickingOrder", events.get(0).getEntityName());
        assertEquals(PickingOrderStatusEnum.DISPATCHED.name(), events.get(0).getNewStatus());
        assertEquals(order.getPickingOrderNo(), events.get(0).getEntityNo());
    }

    @Test
    void cancel_emitsLifecycleEventWithEntityNameAndNo() {
        PickingOrder order = PickingOrder.create(1, false, "WH", 1L, "W1", List.of(), false);
        order.setPickingOrderStatus(PickingOrderStatusEnum.NEW);
        order.clearEvents();

        order.cancel();

        List<LifeCycleStatusChangeEvent> events = lifecycleEvents(order);
        assertEquals(1, events.size());
        assertEquals("PickingOrder", events.get(0).getEntityName());
        assertEquals(PickingOrderStatusEnum.CANCELED.name(), events.get(0).getNewStatus());
        assertTrue(order.getPickingOrderNo().equals(events.get(0).getEntityNo()));
    }
}
