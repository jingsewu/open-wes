package org.openwes.plugin.sdk.controller;

import org.junit.jupiter.api.Test;
import org.openwes.plugin.sdk.service.PluginService;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginControllerTest {

    @Test
    void list_returnsDistinctMapsPerPlugin() {
        PluginManager pluginManager = mock(PluginManager.class);
        PluginWrapper p1 = mock(PluginWrapper.class);
        when(p1.getPluginId()).thenReturn("plugin-a");
        when(p1.getPluginPath()).thenReturn(Path.of("/plugins/wes/jar/a.jar"));
        when(p1.getPluginState()).thenReturn(PluginState.STARTED);
        PluginWrapper p2 = mock(PluginWrapper.class);
        when(p2.getPluginId()).thenReturn("plugin-b");
        when(p2.getPluginPath()).thenReturn(Path.of("/plugins/wes/jar/b.jar"));
        when(p2.getPluginState()).thenReturn(PluginState.STOPPED);
        when(pluginManager.getPlugins()).thenReturn(List.of(p1, p2));

        PluginController controller = new PluginController(mock(PluginService.class), pluginManager);

        List<Map<String, Object>> result = controller.list();

        assertEquals(2, result.size());
        assertNotSame(result.get(0), result.get(1));
        assertEquals("plugin-a", result.get(0).get("pluginId"));
        assertEquals("plugin-b", result.get(1).get("pluginId"));
        assertEquals(PluginState.STARTED, result.get(0).get("pluginStatus"));
        assertEquals(PluginState.STOPPED, result.get(1).get("pluginStatus"));
    }
}
