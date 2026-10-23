package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginConfigServiceTest {

    @Mock
    private ScriptPluginService pluginService;
    @Mock
    private PluginWorkbenchAccessService accessService;
    @Mock
    private ScriptPluginOperationAuditService auditService;

    private ScriptPluginConfigService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginConfigService(pluginService, accessService, auditService);
    }

    @Test
    void getConfig_requiresAdminAndDelegates() {
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO(Map.of(), List.of(), 3L);
        when(accessService.requireAdmin()).thenReturn("admin");
        when(pluginService.getConfig("packing-wave")).thenReturn(dto);

        assertSame(dto, service.getConfig("packing-wave"));
    }

    @Test
    void saveConfig_auditsFieldNamesOnly() {
        ScriptPluginConfigSaveDTO request = new ScriptPluginConfigSaveDTO();
        request.setValues(Map.of("maxPerWave", 25, "enabled", true));
        request.setLockVersion(3L);
        ScriptPluginConfigDTO saved = new ScriptPluginConfigDTO(request.getValues(), List.of(), 4L);
        when(accessService.requireAdmin("packing-wave", ScriptPluginAuditActionEnum.CONFIG_SAVE))
                .thenReturn("admin");
        when(pluginService.saveConfig("packing-wave", request.getValues(), 3L)).thenReturn(saved);

        assertSame(saved, service.saveConfig("packing-wave", request));

        verify(auditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.CONFIG_SAVE), eq(null), eq(true),
                eq("saved config fields: enabled, maxPerWave"), eq("admin"));
    }
}
