package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDisableDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginContractService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginControllerTest {

    private final ScriptPluginWorkbenchService workbenchService =
            mock(ScriptPluginWorkbenchService.class);
    private final PluginWorkbenchAccessService accessService =
            mock(PluginWorkbenchAccessService.class);
    private final ScriptPluginContractService contractService =
            mock(ScriptPluginContractService.class);
    private final ScriptPluginController controller =
            new ScriptPluginController(workbenchService, accessService, contractService);

    @Test
    void list_wrapsServiceResultInSuccessResponse() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setStatus(ScriptPluginStatusEnum.DRAFT);
        when(workbenchService.list()).thenReturn(List.of(dto));

        Response<List<ScriptPluginDTO>> response = controller.list();

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("packing-wave", response.getData().get(0).getPluginKey());
    }

    @Test
    void capabilities_returnsCurrentUserCapabilities() {
        PluginCapabilitiesDTO dto = new PluginCapabilitiesDTO(true, false, Set.of("pluginDeveloper"));
        when(accessService.capabilities()).thenReturn(dto);

        assertEquals(dto, controller.capabilities().getData());
    }

    @Test
    void contract_delegatesWithApplySystem() {
        PluginContractDTO dto = new PluginContractDTO();
        when(contractService.contract("wes")).thenReturn(dto);

        assertEquals(dto, controller.contract("wes").getData());
    }

    @Test
    void enable_delegatesToWorkbenchService() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.enable("packing-wave")).thenReturn(dto);

        Response<ScriptPluginDTO> response = controller.enable("packing-wave");

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        verify(workbenchService).enable("packing-wave");
    }

    @Test
    void disable_passesOptionalReason() {
        ScriptPluginDisableDTO body = new ScriptPluginDisableDTO();
        body.setReason("maintenance window");
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.disable("packing-wave", "maintenance window")).thenReturn(dto);

        controller.disable("packing-wave", body);

        verify(workbenchService).disable("packing-wave", "maintenance window");
    }

    @Test
    void disable_withoutBody_passesNullReason() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.disable("packing-wave", null)).thenReturn(dto);

        controller.disable("packing-wave", null);

        verify(workbenchService).disable("packing-wave", null);
    }

    @Test
    void metadata_delegatesToWorkbenchService() {
        ScriptPluginMetadataDTO body = new ScriptPluginMetadataDTO();
        body.setName("New name");
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.updateMetadata("packing-wave", body)).thenReturn(dto);

        assertEquals(dto, controller.metadata("packing-wave", body).getData());
    }

    @Test
    void audit_delegatesWithPagination() {
        controller.audit("packing-wave", 2, 50);
        controller.allAudit(3, 10);

        verify(workbenchService).auditForPlugin("packing-wave", 2, 50);
        verify(workbenchService).allAudit(3, 10);
    }
}
