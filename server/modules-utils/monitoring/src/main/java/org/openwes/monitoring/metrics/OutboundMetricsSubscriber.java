package org.openwes.monitoring.metrics;

import com.google.common.eventbus.Subscribe;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderCompletionEvent;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderCreatedEvent;
import org.openwes.wes.api.outbound.event.OutboundWaveCompletionEvent;
import org.openwes.wes.api.outbound.event.OutboundWaveCreatedEvent;
import org.openwes.wes.api.outbound.event.PickingOrderCompletionEvent;
import org.openwes.wes.api.outbound.event.PickingOrderDispatchedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboundMetricsSubscriber {

    private final MeterRegistry registry;

    @Subscribe
    public void onOrderCreated(OutboundPlanOrderCreatedEvent event) {
        registry.counter("wes.outbound.orders.created.total").increment();
    }

    @Subscribe
    public void onOrderCompleted(OutboundPlanOrderCompletionEvent event) {
        registry.counter("wes.outbound.orders.completed.total").increment();
    }

    @Subscribe
    public void onPickingDispatched(PickingOrderDispatchedEvent event) {
        registry.counter("wes.outbound.picking.dispatched.total").increment();
    }

    @Subscribe
    public void onPickingCompleted(PickingOrderCompletionEvent event) {
        registry.counter("wes.outbound.picking.completed.total").increment();
    }

    @Subscribe
    public void onWaveCreated(OutboundWaveCreatedEvent event) {
        registry.counter("wes.outbound.waves.created.total").increment();
    }

    @Subscribe
    public void onWaveCompleted(OutboundWaveCompletionEvent event) {
        registry.counter("wes.outbound.waves.completed.total").increment();
    }
}
