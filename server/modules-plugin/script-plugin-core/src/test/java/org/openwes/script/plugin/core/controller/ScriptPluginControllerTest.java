package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginControllerTest {

    private final ScriptPluginService service = mock(ScriptPluginService.class);
    private final ScriptPluginController controller = new ScriptPluginController(service);

    @Test
    void list_wrapsServiceResultInSuccessResponse() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setStatus(ScriptPluginStatusEnum.DRAFT);
        when(service.list()).thenReturn(List.of(dto));

        Response<List<ScriptPluginDTO>> response = controller.list();

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("packing-wave", response.getData().get(0).getPluginKey());
    }

    @Test
    void enable_delegatesToService() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(service.enable("packing-wave")).thenReturn(dto);

        Response<ScriptPluginDTO> response = controller.enable("packing-wave");

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        verify(service).enable("packing-wave");
    }

    @Test
    void disable_passesUserReason() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(service.disable("packing-wave", "disabled by user")).thenReturn(dto);

        controller.disable("packing-wave");

        verify(service).disable("packing-wave", "disabled by user");
    }
}
