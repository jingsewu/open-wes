package org.openwes.wes.inbound.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.wes.api.inbound.IEmptyContainerInboundOrderApi;
import org.openwes.wes.api.inbound.event.EmptyContainerInboundOrderCompletionEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmptyContainerInboundOrderSubscriber {

    private final IEmptyContainerInboundOrderApi emptyContainerInboundOrderApi;

    @Subscribe
    public void onAccept(@Valid EmptyContainerInboundOrderCompletionEvent event) {
        emptyContainerInboundOrderApi.completeDetails(event.getEmptyContainerInboundOrderDetailIds());
    }
}
