package org.openwes.plugin.sdk.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;
import org.pf4j.PluginManager;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class LifecycleListenerRegistryTest {

    @Mock
    private PluginManager pluginManager;
    @Mock
    private LifecycleListenerRegistry registry;

    @BeforeEach
    void setUp() {
        openMocks(this); // 初始化 Mockito 注解
        pluginManager = mock(PluginManager.class);
        registry = new LifecycleListenerRegistry(pluginManager);
    }

    @Test
    void fireAfterStatusChange_withMatchingListener_shouldCallAfterStatusChange() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));

        registry.fireAfterStatusChange("TestEntity", "statusData", "NEW_STATUS");

        verify(listener).afterStatusChange("statusData", "NEW_STATUS");
    }
}
