package org.openwes.plugin.sdk.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LifecycleListenerRegistryTest {

    @Mock
    private PluginManager pluginManager;

    @Test
    void fireAfterStatusChange_withMatchingJarListener_callsAfterStatusChange() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));
        LifecycleListenerRegistry registry = new LifecycleListenerRegistry(pluginManager, List.of());

        registry.fireAfterStatusChange("TestEntity", 1L, "statusData", "NEW_STATUS");

        verify(listener).afterStatusChange(1L, "statusData", "NEW_STATUS");
    }

    @Test
    void fireAfterStatusChange_mergesScriptProvidedListeners() {
        IEntityLifecycleListener scriptListener = mock(IEntityLifecycleListener.class);
        when(scriptListener.getEntityName()).thenReturn("TestEntity");
        ScriptExtensionProvider provider = mock(ScriptExtensionProvider.class);
        when(provider.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(scriptListener));
        LifecycleListenerRegistry registry = new LifecycleListenerRegistry(pluginManager, List.of(provider));

        registry.fireAfterStatusChange("TestEntity", 1L, "statusData", "NEW_STATUS");

        verify(scriptListener).afterStatusChange(1L, "statusData", "NEW_STATUS");
    }

    @Test
    void fireAfterStatusChange_ignoresListenersOfOtherEntities() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("OtherEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));
        LifecycleListenerRegistry registry = new LifecycleListenerRegistry(pluginManager, List.of());

        registry.fireAfterStatusChange("TestEntity", 1L, "statusData", "NEW_STATUS");

        verify(listener, org.mockito.Mockito.never()).afterStatusChange(1L, "statusData", "NEW_STATUS");
    }
}
