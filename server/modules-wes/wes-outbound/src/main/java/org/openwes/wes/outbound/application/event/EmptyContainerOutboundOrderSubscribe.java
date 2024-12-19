package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.outbound.event.EmptyContainerOutboundOrderCompletionEvent;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmptyContainerOutboundOrderSubscribe {

    private final EmptyContainerOutboundOrderRepository emptyContainerOutboundOrderRepository;

    @Subscribe
    public void onComplete(@Valid EmptyContainerOutboundOrderCompletionEvent event) {

        List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders = emptyContainerOutboundOrderRepository.findOrderByDetailIds(event.getEmptyContainerOutboundOrderDetailIds());

        emptyContainerOutboundOrders.forEach(emptyContainerOutboundOrder ->
                emptyContainerOutboundOrder.complete(event.getEmptyContainerOutboundOrderDetailIds()));

        emptyContainerOutboundOrderRepository.saveAllOrderAndDetails(emptyContainerOutboundOrders);
    }
}
