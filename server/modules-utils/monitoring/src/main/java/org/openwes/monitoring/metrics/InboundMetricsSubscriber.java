package org.openwes.monitoring.metrics;

import com.google.common.eventbus.Subscribe;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.inbound.event.AcceptOrderCompletionEvent;
import org.openwes.wes.api.inbound.event.InboundOrderCompletionEvent;
import org.openwes.wes.api.inbound.event.InboundPlanOrderAcceptedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InboundMetricsSubscriber {

    private final MeterRegistry registry;

    @Subscribe
    public void onAccepted(InboundPlanOrderAcceptedEvent event) {
        registry.counter("wes.inbound.orders.accepted.total",
                "warehouse", event.getWarehouseCode() != null ? event.getWarehouseCode() : "unknown")
                .increment();
        if (event.getQtyAccepted() != null) {
            registry.counter("wes.inbound.qty.accepted.total",
                    "warehouse", event.getWarehouseCode() != null ? event.getWarehouseCode() : "unknown")
                    .increment(event.getQtyAccepted());
        }
    }

    @Subscribe
    public void onCompleted(InboundOrderCompletionEvent event) {
        registry.counter("wes.inbound.orders.completed.total").increment();
    }

    @Subscribe
    public void onAcceptCompleted(AcceptOrderCompletionEvent event) {
        registry.counter("wes.inbound.accept.completed.total").increment();
    }
}
