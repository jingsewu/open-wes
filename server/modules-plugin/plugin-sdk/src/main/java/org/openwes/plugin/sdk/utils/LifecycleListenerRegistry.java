package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LifecycleListenerRegistry {

    private final PluginManager pluginManager;

    public <U> void fireAfterStatusChange(String entityName, U u, String status) {

        List<IEntityLifecycleListener> entityListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);

        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityName)).forEach(listener -> {
            ((IEntityLifecycleListener<U>) listener).afterStatusChange(u, status);
        });
    }
}
