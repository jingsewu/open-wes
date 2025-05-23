package org.openwes.wes.outbound.domain.aggregate;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.outbound.domain.context.OutboundPlanOrderCancelContext;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 因为是领域对象方法中会推送事件，所以需要使用事务包裹，必须在事务提交之后推送领域事件
 */
@Service
@RequiredArgsConstructor
public class OutboundPlanOrderAggregate {

    private final OutboundPlanOrderPreAllocatedAggregate outboundPlanOrderPreAllocatedAggregate;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final PickingOrderRepository pickingOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void cancel(OutboundPlanOrderCancelContext outboundPlanOrderCancelContext) {

        outboundPlanOrderCancelContext.getOutboundPlanOrders().forEach(OutboundPlanOrder::cancel);
        outboundPlanOrderRepository.saveOrderAndDetails(outboundPlanOrderCancelContext.getOutboundPlanOrders());

        outboundPlanOrderPreAllocatedAggregate.cancel(outboundPlanOrderCancelContext.getOutboundPreAllocatedRecords());

        outboundPlanOrderCancelContext.getOutboundWaves().forEach(OutboundWave::cancel);
        outboundWaveRepository.saveAll(outboundPlanOrderCancelContext.getOutboundWaves());

        outboundPlanOrderCancelContext.getPickingOrders().forEach(PickingOrder::cancel);
        pickingOrderRepository.saveOrderAndDetails(outboundPlanOrderCancelContext.getPickingOrders());

    }
}
