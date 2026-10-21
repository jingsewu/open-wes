package org.openwes.script.plugin.runtime.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.materialize.ScriptPluginMaterializer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginRegistryTest {

    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final ScriptPluginMaterializer materializer = mock(ScriptPluginMaterializer.class);
    private final RedisUtils redisUtils = mock(RedisUtils.class);
    private ScriptPluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ScriptPluginRegistry(accessor, materializer, redisUtils, "wes");
    }

    private ScriptPluginBundleDTO bundle(long revisionId) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setApplySystem("wes");
        bundle.setRevisionId(revisionId);
        return bundle;
    }

    @Test
    void refreshAll_materializesEnabledBundlesAndClosesSupersededRevisions() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        MaterializedScriptPlugin materialized = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(accessor.getRegistryVersion("wes")).thenReturn(5L);
        when(materializer.materialize(bundle)).thenReturn(materialized);

        registry.refreshAll();

        assertSame(materialized, registry.getMaterialized("packing-wave"));
        verify(materializer).closeRevisionsExcept("packing-wave", 11L);
    }

    @Test
    void refreshAll_unchangedRevision_isNotRematerialized() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(materializer.materialize(bundle)).thenReturn(new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>()));

        registry.refreshAll();
        registry.refreshAll();

        verify(materializer, times(1)).materialize(bundle);
    }

    @Test
    void refreshAll_failedRematerialization_keepsThePreviousRevision() {
        ScriptPluginBundleDTO first = bundle(11L);
        MaterializedScriptPlugin materialized = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        when(materializer.materialize(first)).thenReturn(materialized);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(first));
        registry.refreshAll();

        ScriptPluginBundleDTO second = bundle(12L);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(second));
        when(materializer.materialize(second)).thenThrow(new IllegalStateException("compile failed"));
        registry.refreshAll();

        assertSame(materialized, registry.getMaterialized("packing-wave"));
        verify(materializer, never()).closeRevisionsExcept("packing-wave", 12L);
    }

    @Test
    void refreshAll_failedRematerialization_discardsTheFailedRevisionAndKeepsThePreviousOne() {
        ScriptPluginBundleDTO first = bundle(11L);
        MaterializedScriptPlugin materialized = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        when(materializer.materialize(first)).thenReturn(materialized);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(first));
        registry.refreshAll();

        ScriptPluginBundleDTO second = bundle(12L);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(second));
        when(materializer.materialize(second)).thenThrow(new IllegalStateException("compile failed"));
        registry.refreshAll();

        assertSame(materialized, registry.getMaterialized("packing-wave"));
        verify(materializer, times(2)).closeRevisionsExcept("packing-wave", 11L);
    }

    @Test
    void refreshAll_firstEverMaterializationFailure_closesThePluginContext() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(materializer.materialize(bundle)).thenThrow(new IllegalStateException("compile failed"));

        registry.refreshAll();

        assertNull(registry.getMaterialized("packing-wave"));
        verify(materializer).closePlugin("packing-wave");
    }

    @Test
    void refreshAll_pluginNoLongerEnabled_isDematerialized() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        when(materializer.materialize(bundle)).thenReturn(new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>()));
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        registry.refreshAll();

        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of());
        registry.refreshAll();

        assertNull(registry.getMaterialized("packing-wave"));
        verify(materializer).closePlugin("packing-wave");
    }

    @Test
    void pollForUpdates_refreshesOnlyWhenTheVersionChanged() {
        when(accessor.getRegistryVersion("wes")).thenReturn(5L);

        registry.pollForUpdates();
        verify(accessor, times(1)).getEnabledPlugins("wes");

        registry.pollForUpdates();
        verify(accessor, times(1)).getEnabledPlugins("wes");

        when(accessor.getRegistryVersion("wes")).thenReturn(6L);
        registry.pollForUpdates();
        verify(accessor, times(2)).getEnabledPlugins("wes");
    }

    @Test
    void pollForUpdates_whenManagementPlaneUnavailable_isSwallowed() {
        when(accessor.getRegistryVersion("wes")).thenThrow(new RuntimeException("dubbo down"));

        registry.pollForUpdates();

        verify(accessor, never()).getEnabledPlugins(any());
    }
}
