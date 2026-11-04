package org.openwes.script.plugin.api;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ScriptPluginContentHasherTest {

    private ExtensionDeclarationDTO declaration() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        declaration.setPriority(100);
        return declaration;
    }

    @Test
    void hash_isStableForSameContent() {
        String first = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src"));
        String second = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src"));

        assertEquals(first, second);
    }

    @Test
    void hash_changesWhenScriptChanges() {
        String first = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src"));
        String second = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src2"));

        assertNotEquals(first, second);
    }

    @Test
    void hash_changesWhenPriorityChanges() {
        ExtensionDeclarationDTO changed = declaration();
        changed.setPriority(1);
        assertNotEquals(
                ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src")),
                ScriptPluginContentHasher.hash(List.of(changed), Map.of("Wave.groovy", "src")));
    }
}
