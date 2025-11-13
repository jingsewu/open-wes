package org.openwes.wes.ems.proxy.application.event;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.ems.proxy.event.ContainerTaskUpdatedStatusEvent;
import org.openwes.wes.api.inbound.IEmptyContainerInboundOrderApi;
import org.openwes.wes.api.inbound.IPutAwayTaskApi;
import org.openwes.wes.api.outbound.IEmptyContainerOutboundOrderApi;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContainerTaskEventSubscribe {

    private final IEmptyContainerInboundOrderApi emptyContainerInboundOrderApi;
    private final IEmptyContainerOutboundOrderApi emptyContainerOutboundOrderApi;
    private final IPutAwayTaskApi putAwayTaskApi;

    @Subscribe
    public void onContainerTaskStatusUpdatedEvent(ContainerTaskUpdatedStatusEvent event) {
        switch (event.getBusinessTaskType()) {
            case EMPTY_CONTAINER_INBOUND:
                emptyContainerInboundOrderApi.completeDetails(event.getRelationTaskIds());
                break;
            case EMPTY_CONTAINER_OUTBOUND:
                emptyContainerOutboundOrderApi.completeDetails(event.getRelationTaskIds());
                break;

            case PUT_AWAY:
                putAwayTaskApi.complete(event.getRelationTaskIds(), event.getLocationCode());
                break;
            case PICKING:
                log.debug("Picking task completed: {}", event.getRelationTaskIds());
                break;
            default:
                log.warn("Unknown business task type: {}", event.getBusinessTaskType());
                break;
        }
    }
}
