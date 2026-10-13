package org.openwes.script.plugin.api.contract;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.enums.ExtensionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginContractCatalogTest {

    @Test
    void everyExtensionType_hasAnArchetypeWithAScriptMethod() {
        for (ExtensionType type : ExtensionType.values()) {
            PluginContractCatalog.ArchetypeDescriptor archetype = PluginContractCatalog.archetype(type).orElseThrow();
            assertEquals(type, archetype.type());
            assertFalse(archetype.scriptMethod().isBlank());
        }
    }

    @Test
    void archetypeExtensionPointClasses_resolveAndDeclareTheScriptMethod() throws Exception {
        for (PluginContractCatalog.ArchetypeDescriptor archetype : PluginContractCatalog.archetypes()) {
            if (archetype.extensionPointClassName() == null) {
                continue; // dispatcher-backed archetypes (HTTP_ENDPOINT, CRON)
            }
            Class<?> extensionPoint = Class.forName(archetype.extensionPointClassName());
            // wave/split/validate/... must be declared somewhere in the interface hierarchy
            assertTrue(declaresMethod(extensionPoint, archetype.scriptMethod()),
                    extensionPoint.getName() + " must declare " + archetype.scriptMethod());
        }
    }

    private boolean declaresMethod(Class<?> clazz, String methodName) {
        Class<?> current = clazz;
        while (current != null) {
            for (java.lang.reflect.Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    @Test
    void beanCatalog_listsResolvableTypesPerApp() throws Exception {
        assertEquals(2, PluginContractCatalog.beanCatalog().size());
        for (var entry : PluginContractCatalog.beanCatalog().entrySet()) {
            assertFalse(entry.getValue().isEmpty(), entry.getKey() + " must ship a bean catalog");
            for (PluginContractCatalog.BeanDescriptor bean : entry.getValue()) {
                Class.forName(bean.type().getName());
            }
        }
    }

    @Test
    void promptFragment_containsOnlyRequestedArchetypes() {
        String fragment = PluginContractCatalog.toPromptFragment("wes",
                java.util.List.of(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, ExtensionType.CRON));

        assertTrue(fragment.contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertTrue(fragment.contains("CRON"));
        assertFalse(fragment.contains("CUSTOM_API"));
        assertTrue(fragment.contains("ctx."));
    }
}
