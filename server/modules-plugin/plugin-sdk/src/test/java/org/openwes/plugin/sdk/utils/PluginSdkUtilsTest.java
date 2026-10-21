package org.openwes.plugin.sdk.utils;

import org.junit.jupiter.api.Test;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginSdkUtilsTest {

    private final PluginManager pluginManager = mock(PluginManager.class);

    @Test
    void getExtractObject_mergesScriptExtensionsAndSortsByPriorityDescending() {
        IOutboundWaveWaveAction jarPlugin = mock(IOutboundWaveWaveAction.class);
        when(jarPlugin.priority()).thenReturn(10);
        IOutboundWaveWaveAction scriptPlugin = mock(IOutboundWaveWaveAction.class);
        when(scriptPlugin.priority()).thenReturn(100);
        when(pluginManager.getExtensions(IOutboundWaveWaveAction.class)).thenReturn(List.of(jarPlugin));
        ScriptExtensionProvider provider = mock(ScriptExtensionProvider.class);
        when(provider.getExtensions(IOutboundWaveWaveAction.class)).thenReturn(List.of(scriptPlugin));

        PluginSdkUtils utils = new PluginSdkUtils(pluginManager, List.of(provider));

        assertEquals(List.of(scriptPlugin, jarPlugin), utils.getExtractObject(IOutboundWaveWaveAction.class));
    }

    @Test
    void getExtractObject_withoutScriptProviders_keepsJarPlugins() {
        IOutboundWaveWaveAction jarPlugin = mock(IOutboundWaveWaveAction.class);
        when(pluginManager.getExtensions(IOutboundWaveWaveAction.class)).thenReturn(List.of(jarPlugin));

        PluginSdkUtils utils = new PluginSdkUtils(pluginManager, List.of());

        assertEquals(List.of(jarPlugin), utils.getExtractObject(IOutboundWaveWaveAction.class));
    }

    @Test
    void getExtractObject_nothingRegistered_returnsEmptyList() {
        PluginSdkUtils utils = new PluginSdkUtils(pluginManager, List.of());

        assertTrue(utils.getExtractObject(IOutboundWaveWaveAction.class).isEmpty());
    }
}
