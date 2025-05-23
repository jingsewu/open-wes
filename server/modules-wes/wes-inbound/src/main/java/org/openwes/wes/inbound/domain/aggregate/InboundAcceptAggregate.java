package org.openwes.wes.inbound.domain.aggregate;

import lombok.RequiredArgsConstructor;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.inbound.event.PutAwayCreationEvent;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InboundAcceptAggregate {

    private final InboundPlanOrderRepository inboundPlanOrderRepository;
    private final AcceptOrderRepository acceptOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void cancelAccept(AcceptOrder acceptOrder, Long acceptOrderDetailId, List<InboundPlanOrder> inboundPlanOrders) {

        List<AcceptOrderDetail> canceledDetails = acceptOrder.getDetails().stream()
                .filter(v -> acceptOrderDetailId == null || v.getId().equals(acceptOrderDetailId))
                .toList();
        inboundPlanOrders.forEach(inboundPlanOrder -> canceledDetails.forEach(canceledDetail -> {
            if (canceledDetail.getInboundPlanOrderId().equals(inboundPlanOrder.getId())) {
                inboundPlanOrder.cancelAccept(canceledDetail.getInboundPlanOrderDetailId(), canceledDetail.getQtyAccepted());
            }
        }));

        inboundPlanOrderRepository.saveAllOrdersAndDetails(inboundPlanOrders);

        acceptOrder.cancel(acceptOrderDetailId);
        acceptOrderRepository.saveOrderAndDetail(acceptOrder);

    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(AcceptOrder acceptOrder, List<InboundPlanOrder> inboundPlanOrders) {
        acceptOrder.complete();
        acceptOrderRepository.saveOrder(acceptOrder);

        inboundPlanOrders.stream().filter(InboundPlanOrder::isFullAccepted).forEach(InboundPlanOrder::completeAccepted);
        inboundPlanOrderRepository.saveOrders(inboundPlanOrders);

        DomainEventPublisher.sendAsyncDomainEvent(new PutAwayCreationEvent().setAcceptOrderId(acceptOrder.getId()));
    }
}
