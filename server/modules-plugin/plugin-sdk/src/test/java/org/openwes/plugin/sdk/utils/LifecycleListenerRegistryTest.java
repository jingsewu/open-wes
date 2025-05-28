package org.openwes.plugin.sdk.utils;

import com.open.wes.extension.business.IEntityLifecycleListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginManager;

import java.util.Collections;
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
    void fireBeforeCreate_withMatchingListener_shouldCallBeforeCreate() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));

        registry.fireBeforeCreate("TestEntity", "createData");

        verify(listener).beforeCreate("createData");
    }

    @Test
    void fireBeforeCreate_withNoMatchingListener_shouldDoNothing() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("OtherEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));

        registry.fireBeforeCreate("TestEntity", "createData");

        verify(listener, never()).beforeCreate(any());
    }

    @Test
    void fireBeforeCreate_withEmptyListeners_shouldDoNothing() {
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(Collections.emptyList());

        registry.fireBeforeCreate("TestEntity", "createData");

        // 不做任何验证，确保不会抛异常即可
    }

    @Test
    void fireAfterCreate_withMatchingListener_shouldCallAfterCreate() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));

        registry.fireAfterCreate("TestEntity", "afterCreateData");

        verify(listener).afterCreate("afterCreateData");
    }

    @Test
    void fireBeforeCancel_withMatchingListener_shouldCallBeforeCancel() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));

        registry.fireBeforeCancel("TestEntity", "cancelData");

        verify(listener).beforeCancel("cancelData");
    }

    @Test
    void fireAfterCancel_withMatchingListener_shouldCallAfterCancel() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));

        registry.fireAfterCancel("TestEntity", "afterCancelData");

        verify(listener).afterCancel("afterCancelData");
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
