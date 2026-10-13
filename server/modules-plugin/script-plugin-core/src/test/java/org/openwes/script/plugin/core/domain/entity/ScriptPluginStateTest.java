package org.openwes.script.plugin.core.domain.entity;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptPluginStateTest {

    private ScriptPlugin plugin(ScriptPluginStatusEnum status) {
        ScriptPlugin plugin = new ScriptPlugin();
        plugin.setPluginKey("packing-wave");
        plugin.setStatus(status);
        return plugin;
    }

    @Test
    void enable_fromDraft_ok() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.DRAFT);
        plugin.enable();
        assertEquals(ScriptPluginStatusEnum.ENABLED, plugin.getStatus());
    }

    @Test
    void enable_fromDisabled_ok() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.DISABLED);
        plugin.enable();
        assertEquals(ScriptPluginStatusEnum.ENABLED, plugin.getStatus());
    }

    @Test
    void enable_fromEnabled_throws() {
        assertThrows(IllegalStateException.class, () -> plugin(ScriptPluginStatusEnum.ENABLED).enable());
    }

    @Test
    void disable_requiresEnabled() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.ENABLED);
        plugin.disable();
        assertEquals(ScriptPluginStatusEnum.DISABLED, plugin.getStatus());
        assertThrows(IllegalStateException.class, () -> plugin(ScriptPluginStatusEnum.DRAFT).disable());
    }

    @Test
    void archive_fromAnyNonArchivedState_ok() {
        ScriptPlugin draft = plugin(ScriptPluginStatusEnum.DRAFT);
        draft.archive();
        assertEquals(ScriptPluginStatusEnum.ARCHIVED, draft.getStatus());
        ScriptPlugin enabled = plugin(ScriptPluginStatusEnum.ENABLED);
        enabled.archive();
        assertEquals(ScriptPluginStatusEnum.ARCHIVED, enabled.getStatus());
    }

    @Test
    void archive_isTerminal() {
        assertThrows(IllegalStateException.class, () -> plugin(ScriptPluginStatusEnum.ARCHIVED).archive());
    }
}
