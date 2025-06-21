package org.openwes.plugin.sdk.application.event;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.api.dto.event.LifeCycleStatusChangeEvent;
import org.openwes.plugin.sdk.utils.LifecycleListenerRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LifeCycleStatusChangeEventSubscribe {

    private final LifecycleListenerRegistry lifecycleListenerRegistry;

    @Subscribe
    public void onEvent(LifeCycleStatusChangeEvent event) {
        log.info("receive entity: {} status change event: {}", event.getEntityName(), event);
        lifecycleListenerRegistry.fireAfterStatusChange(event.getEntityName(),
                event.getEntityId(), event.getEntityNo(), event.getNewStatus());
    }
}
