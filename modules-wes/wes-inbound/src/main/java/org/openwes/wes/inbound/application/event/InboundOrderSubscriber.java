package org.openwes.wes.inbound.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.wes.api.inbound.event.InboundOrderCompletionEvent;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;
import org.openwes.wes.inbound.domain.transfer.InboundPlanOrderTransfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InboundOrderSubscriber {

    private final InboundPlanOrderRepository inboundPlanOrderRepository;
    private final InboundPlanOrderTransfer inboundPlanOrderTransfer;
    private final CallbackApiFacade callbackApiFacade;

    @Subscribe
    public void onCompletion(@Valid InboundOrderCompletionEvent event) {
        InboundPlanOrder inboundPlanOrder = inboundPlanOrderRepository.findById(event.getInboundOrderId());
        callbackApiFacade.callback(CallbackApiTypeEnum.INBOUND_PLAN_ORDER_COMPLETED,
                inboundPlanOrder.getCustomerOrderType(), inboundPlanOrderTransfer.toDTO(inboundPlanOrder));
    }
}
