package org.openwes.wes.outbound.domain.entity;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.utils.ObjectUtils;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

class OutboundPlanOrderTest {

    @Test
    void testInitialize() {
        RedisUtils mockRedisUtils = Mockito.mock(RedisUtils.class);
        Mockito.when(mockRedisUtils.getAndIncrement(anyString(), anyInt())).thenReturn(10L);
        new OrderNoGenerator(mockRedisUtils);

        OutboundPlanOrder randomObject = ObjectUtils.getRandomObject(OutboundPlanOrder.class);
        randomObject.initialize();

        Assertions.assertEquals((int) randomObject.getSkuKindNum(),
                randomObject.getDetails().stream().map(OutboundPlanOrderDetail::getSkuCode).distinct().count());
        Assertions.assertEquals(randomObject.getTotalQty(),
                randomObject.getDetails().stream().map(OutboundPlanOrderDetail::getQtyRequired).reduce(Integer::sum).orElse(0));

        Assertions.assertTrue(StringUtils.contains(randomObject.getOrderNo(), "OUT_"));
    }

    @Test
    void testInitSkuId() {
        OutboundPlanOrder randomObject = ObjectUtils.getRandomObject(OutboundPlanOrder.class);

        SkuMainDataDTO skuMainDataDTO = ObjectUtils.getRandomObject(SkuMainDataDTO.class);
        skuMainDataDTO.setSkuCode(randomObject.getDetails().iterator().next().getSkuCode());
        skuMainDataDTO.setWarehouseCode(randomObject.getWarehouseCode());
        skuMainDataDTO.setOwnerCode(randomObject.getDetails().iterator().next().getOwnerCode());

        randomObject.initSkuInfo(Sets.newHashSet(skuMainDataDTO));

        Assertions.assertEquals(randomObject.getDetails().iterator().next().getSkuId(), skuMainDataDTO.getId());
    }

    @Test
    void testPreAllocate() {
        OutboundPlanOrder randomObject = ObjectUtils.getRandomObject(OutboundPlanOrder.class);

        // allocate qty over requirement
        OutboundPreAllocatedRecord overRecord = OutboundPreAllocatedRecord.builder()
                .qtyPreAllocated(Integer.MAX_VALUE)
                .build();
        List<OutboundPreAllocatedRecord> records = Lists.newArrayList(overRecord);
        Assertions.assertThrows(IllegalArgumentException.class, () -> randomObject.preAllocate(records));

        // requirement matching
        OutboundPreAllocatedRecord matchRecord = OutboundPreAllocatedRecord.builder()
                .outboundPlanOrderDetailId(randomObject.getDetails().iterator().next().getId())
                .qtyPreAllocated(randomObject.getDetails().iterator().next().getQtyRequired())
                .build();
        boolean result = randomObject.preAllocate(Lists.newArrayList(matchRecord));
        Assertions.assertTrue(result);
        Assertions.assertSame(OutboundPlanOrderStatusEnum.ASSIGNED, randomObject.getOutboundPlanOrderStatus());
    }

    @Test
    void testPick() {
        OutboundPlanOrder randomObject = ObjectUtils.getRandomObjectIgnoreFields(OutboundPlanOrder.class, "qtyActual");
        OutboundPlanOrderDetail detail = randomObject.getDetails().iterator().next();

        // Use reflection to set the status needed for picking test
        // ObjectUtils.getRandomObject creates objects via reflection so fields are directly set
        try {
            java.lang.reflect.Field statusField = OutboundPlanOrder.class.getDeclaredField("outboundPlanOrderStatus");
            statusField.setAccessible(true);
            statusField.set(randomObject, OutboundPlanOrderStatusEnum.ASSIGNED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        randomObject.picking(1, detail.getId());
        Assertions.assertEquals(1, detail.getQtyActual());
        Assertions.assertEquals(OutboundPlanOrderStatusEnum.PICKING, randomObject.getOutboundPlanOrderStatus());
    }

}
