package org.openwes.wes.outbound.domain.entity;

import com.google.common.collect.Sets;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.utils.ObjectUtils;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        randomObject.initSkuInfo(Sets.newHashSet(skuMainDataDTO));

        Assertions.assertEquals(randomObject.getDetails().iterator().next().getSkuId(), skuMainDataDTO.getId());
    }

    @Test
    void testPreAllocate() {
        OutboundPlanOrder randomObject = ObjectUtils.getRandomObject(OutboundPlanOrder.class);
        OutboundPreAllocatedRecord preAllocatedRecord = ObjectUtils.getRandomObject(OutboundPreAllocatedRecord.class);

        //allocate qty over requirement
        preAllocatedRecord.setQtyPreAllocated(Integer.MAX_VALUE);
        List<OutboundPreAllocatedRecord> records = Lists.newArrayList(preAllocatedRecord);
        Assertions.assertThrows(IllegalArgumentException.class, () -> randomObject.preAllocate(records));

        //requirement matching
        preAllocatedRecord.setQtyPreAllocated(randomObject.getDetails().iterator().next().getQtyRequired());
        boolean result = randomObject.preAllocate(Lists.newArrayList(preAllocatedRecord));
        Assertions.assertTrue(result);
        Assertions.assertSame(OutboundPlanOrderStatusEnum.ASSIGNED, randomObject.getOutboundPlanOrderStatus());

        //allocate qty less than requirement and not allow short outbound
        randomObject.setShortOutbound(false);
        randomObject.setShortWaiting(false);
        preAllocatedRecord.setQtyPreAllocated(randomObject.getDetails().iterator().next().getQtyRequired() - 1);
        result = randomObject.preAllocate(Lists.newArrayList(preAllocatedRecord));
        Assertions.assertFalse(result);
        Assertions.assertSame(OutboundPlanOrderStatusEnum.PICKED, randomObject.getOutboundPlanOrderStatus());
        Assertions.assertTrue(randomObject.isAbnormal());

        //allocate qty less than requirement and allow short outbound
        randomObject.setShortOutbound(true);
        randomObject.setShortWaiting(true);
        preAllocatedRecord.setQtyPreAllocated(randomObject.getDetails().iterator().next().getQtyRequired() - 1);
        result = randomObject.preAllocate(Lists.newArrayList(preAllocatedRecord));
        Assertions.assertTrue(result);
        Assertions.assertSame(OutboundPlanOrderStatusEnum.SHORT_WAITING, randomObject.getOutboundPlanOrderStatus());
    }

    @Test
    void testPick() {
        OutboundPlanOrder randomObject = ObjectUtils.getRandomObjectIgnoreFields(OutboundPlanOrder.class, "qtyActual");
        OutboundPlanOrderDetail detail = randomObject.getDetails().iterator().next();
        randomObject.setOutboundPlanOrderStatus(OutboundPlanOrderStatusEnum.ASSIGNED);
        randomObject.picking(1, detail.getId());
        Assertions.assertEquals(1, detail.getQtyActual());
        Assertions.assertEquals(OutboundPlanOrderStatusEnum.PICKING, randomObject.getOutboundPlanOrderStatus());
    }

}
