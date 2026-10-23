package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginConfigService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginConfigControllerTest {

    private final ScriptPluginConfigService service = mock(ScriptPluginConfigService.class);
    private final ScriptPluginConfigController controller = new ScriptPluginConfigController(service);

    @Test
    void getConfig_returnsServiceResult() {
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO(Map.of("maxPerWave", 20), List.of(), 3L);
        when(service.getConfig("packing-wave")).thenReturn(dto);

        assertEquals(dto, controller.getConfig("packing-wave").getData());
    }

    @Test
    void saveConfig_delegates() {
        ScriptPluginConfigSaveDTO body = new ScriptPluginConfigSaveDTO();
        body.setValues(Map.of("maxPerWave", 25));
        body.setLockVersion(3L);
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO(body.getValues(), List.of(), 4L);
        when(service.saveConfig("packing-wave", body)).thenReturn(dto);

        assertEquals(dto, controller.saveConfig("packing-wave", body).getData());
        verify(service).saveConfig("packing-wave", body);
    }
}
