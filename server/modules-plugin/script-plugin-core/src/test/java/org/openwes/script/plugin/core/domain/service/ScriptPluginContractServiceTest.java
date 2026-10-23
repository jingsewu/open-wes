package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScriptPluginContractServiceTest {

    @Mock
    private PluginWorkbenchAccessService accessService;

    private ScriptPluginContractService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginContractService(accessService);
    }

    @Test
    void wesContract_excludesStationOnlyArchetypes() {
        PluginContractDTO contract = service.contract("wes");

        assertEquals("1.0", contract.getContractVersion());
        assertEquals("wes", contract.getApplySystem());
        assertTrue(contract.getArchetypes().stream()
                .noneMatch(archetype -> archetype.getType() == ExtensionType.CUSTOM_API));
        assertTrue(contract.getArchetypes().stream()
                .anyMatch(archetype -> archetype.getType() == ExtensionType.CRON));
        assertEquals(PluginContractCatalog.LIFECYCLE_ENTITY_NAMES,
                contract.getLifecycleEntityNames());
        assertTrue(contract.getPromptFragment().contains("Contract version: 1.0"));
        verify(accessService).requireAuthenticated();
    }

    @Test
    void stationContract_containsOnlyStationArchetypes() {
        PluginContractDTO contract = service.contract("station");

        assertEquals(3, contract.getArchetypes().size());
        assertEquals(ExtensionType.CUSTOM_API, contract.getArchetypes().get(0).getType());
    }

    @Test
    void unknownApp_isRejected() {
        assertThrows(CommonException.class, () -> service.contract("unknown"));
    }
}
