package org.openwes.monitoring.metrics;

import com.google.common.eventbus.Subscribe;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.task.event.OperationTaskAbnormalEvent;
import org.openwes.wes.api.task.event.OperationTaskPickedEvent;
import org.openwes.wes.api.task.event.TransferContainerSealedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMetricsSubscriber {

    private final MeterRegistry registry;

    @Subscribe
    public void onTaskPicked(OperationTaskPickedEvent event) {
        registry.counter("wes.task.picked.total").increment();
    }

    @Subscribe
    public void onTaskAbnormal(OperationTaskAbnormalEvent event) {
        registry.counter("wes.task.abnormal.total").increment();
    }

    @Subscribe
    public void onContainerSealed(TransferContainerSealedEvent event) {
        registry.counter("wes.task.container.sealed.total").increment();
    }
}
