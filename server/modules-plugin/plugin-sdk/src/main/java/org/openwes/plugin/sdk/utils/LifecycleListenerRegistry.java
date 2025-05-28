package org.openwes.plugin.sdk.utils;

import com.open.wes.extension.business.IEntityLifecycleListener;
import lombok.RequiredArgsConstructor;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LifecycleListenerRegistry {

    private final PluginManager pluginManager;

    public <C, D, U> void fireBeforeCreate(String entityType, C createDTOs) {

        List<IEntityLifecycleListener> entityListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);

        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityType)).forEach(listener -> {
            ((IEntityLifecycleListener<C, D, U>) listener).beforeCreate(createDTOs);
        });
    }

    public <C, D, U> void fireAfterCreate(String entityType, D createDTOs) {

        List<IEntityLifecycleListener> entityListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);

        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityType)).forEach(listener -> {
            ((IEntityLifecycleListener<C, D, U>) listener).afterCreate(createDTOs);
        });
    }

    public <C, D, U> void fireBeforeCancel(String entityType, U u) {

        List<IEntityLifecycleListener> entityListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);

        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityType)).forEach(listener -> {
            ((IEntityLifecycleListener<C, D, U>) listener).beforeCancel(u);
        });
    }

    public <C, D, U> void fireAfterCancel(String entityType, U u) {

        List<IEntityLifecycleListener> entityListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);

        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityType)).forEach(listener -> {
            ((IEntityLifecycleListener<C, D, U>) listener).afterCancel(u);
        });
    }

    public <C, D, U> void fireAfterStatusChange(String entityType, U u, String status) {

        List<IEntityLifecycleListener> entityListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);

        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityType)).forEach(listener -> {
            ((IEntityLifecycleListener<C, D, U>) listener).afterStatusChange(u, status);
        });
    }
}
