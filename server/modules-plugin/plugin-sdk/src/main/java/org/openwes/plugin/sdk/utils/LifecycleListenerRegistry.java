package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LifecycleListenerRegistry {

    private final PluginManager pluginManager;
    private final List<ScriptExtensionProvider> scriptExtensionProviders;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U, V> void fireAfterStatusChange(String entityName, U u, V v, String status) {

        List<IEntityLifecycleListener> entityListeners = new ArrayList<>();
        List<IEntityLifecycleListener> pluginListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);
        if (pluginListeners != null) {
            entityListeners.addAll(pluginListeners);
        }
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            entityListeners.addAll(provider.getExtensions(IEntityLifecycleListener.class));
        }

        entityListeners.sort(Comparator.comparingInt(LifecycleListenerRegistry::priorityOf).reversed());
        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityName))
                .forEach(listener -> listener.afterStatusChange(u, v, status));
    }

    private static int priorityOf(Object listener) {
        return listener instanceof IPlugin plugin ? plugin.priority() : 0;
    }
}
