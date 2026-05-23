package org.openwes.monitoring.metrics;

import com.google.common.eventbus.Subscribe;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.stock.event.StockClearEvent;
import org.openwes.wes.api.stock.event.StockCreateEvent;
import org.openwes.wes.api.stock.event.StockTransferEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockMetricsSubscriber {

    private final MeterRegistry registry;

    @Subscribe
    public void onStockCreated(StockCreateEvent event) {
        registry.counter("wes.stock.operations.total", "type", "create").increment();
    }

    @Subscribe
    public void onStockTransferred(StockTransferEvent event) {
        registry.counter("wes.stock.operations.total", "type", "transfer",
                "taskType", event.getTaskType() != null ? event.getTaskType().name() : "unknown")
                .increment();
    }

    @Subscribe
    public void onStockCleared(StockClearEvent event) {
        registry.counter("wes.stock.operations.total", "type", "clear").increment();
    }
}
