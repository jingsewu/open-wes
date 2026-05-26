package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImprovePriorityUseCase {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<Long> outboundPlanOrderIds, int priority) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundPlanOrderIds);
        outboundPlanOrders.forEach(v -> v.improvePriority(priority));
        outboundPlanOrderRepository.saveAllOrders(outboundPlanOrders);
    }
}
