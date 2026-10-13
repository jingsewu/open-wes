package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.PluginManagementMessage;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginConfigRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRevisionRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginServiceTest {

    @Mock
    private ScriptPluginRepository pluginRepository;
    @Mock
    private ScriptPluginRevisionRepository revisionRepository;
    @Mock
    private ScriptPluginConfigRepository configRepository;
    @Mock
    private RedisUtils redisUtils;

    private ScriptPluginService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(), redisUtils);
    }

    private ScriptPlugin plugin(String pluginKey, String applySystem, ScriptPluginStatusEnum status) {
        ScriptPlugin plugin = new ScriptPlugin();
        plugin.setId(1L);
        plugin.setPluginKey(pluginKey);
        plugin.setName("Packing wave rule");
        plugin.setApplySystem(applySystem);
        plugin.setApplyModule("");
        plugin.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        plugin.setStatus(status);
        plugin.setExtensionPoints("[]");
        plugin.setCurrentRevisionId(11L);
        return plugin;
    }

    private ScriptPluginRevision revision() {
        ScriptPluginRevision revision = new ScriptPluginRevision();
        revision.setId(11L);
        revision.setPluginId(1L);
        revision.setRevisionNo(1);
        revision.setScripts("{}");
        revision.setExtensionPointsSnapshot("[]");
        revision.setContentHash("hash");
        revision.setCreatedAt(System.currentTimeMillis());
        return revision;
    }

    @Test
    void enable_withPassingLocalValidator_enablesAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        ScriptPluginMaterializationValidator validator = mock(ScriptPluginMaterializationValidator.class);
        when(validator.targetApp()).thenReturn("wes");
        when(validator.validate(any())).thenReturn(
                ValidationReportDTO.passed("packing-wave", 11L, "wes"));
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(validator), redisUtils);

        ScriptPluginDTO dto = service.enable("packing-wave");

        assertEquals(ScriptPluginStatusEnum.ENABLED, dto.getStatus());
        assertEquals("PASSED", dto.getLastValidationStatus());
        verify(redisUtils).publish(eq(org.openwes.common.utils.constants.RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT),
                any(PluginManagementMessage.class));
    }

    @Test
    void enable_withFailingLocalValidator_refusesAndStaysDraft() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        ScriptPluginMaterializationValidator validator = mock(ScriptPluginMaterializationValidator.class);
        when(validator.targetApp()).thenReturn("wes");
        when(validator.validate(any())).thenReturn(
                ValidationReportDTO.failed("packing-wave", 11L, "wes", "compile", "line 3: unexpected token"));
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(validator), redisUtils);

        CommonException e = assertThrows(CommonException.class, () -> service.enable("packing-wave"));

        assertTrue(e.getMessage().contains("compile: line 3"));
        assertEquals(ScriptPluginStatusEnum.DRAFT, plugin.getStatus());
        assertEquals("FAILED", plugin.getLastValidationStatus());
        verify(redisUtils, never()).publish(any(), any());
    }

    @Test
    void enable_withWrongSdkVersion_isRefusedBeforeValidation() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setSdkVersion("0.9");
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        assertThrows(CommonException.class, () -> service.enable("packing-wave"));
        verify(revisionRepository, never()).findById(any());
    }

    @Test
    void validate_forRemoteApp_waitsForNodeReport() throws Exception {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ValidationReportDTO> result = executor.submit(() -> service.validate("station-api"));
            for (int i = 0; i < 200 && service.findPendingValidation("station") == null; i++) {
                Thread.sleep(10);
            }
            assertNotNull(service.findPendingValidation("station"), "validation request must be pending for the station poller");

            service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

            assertTrue(result.get(2, TimeUnit.SECONDS).isPassed());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void validate_forRemoteApp_timesOutWhenNodeNeverReports() {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(), redisUtils);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "validationTimeoutMs", 100L);

        ValidationReportDTO report = service.validate("station-api");

        assertTrue(!report.isPassed());
        assertTrue(report.failureSummary().contains("did not report"));
    }

    @Test
    void rollback_movesThePointerAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.ENABLED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        ScriptPluginRevision old = revision();
        old.setId(7L);
        old.setExtensionPointsSnapshot("[{\"type\":\"CRON\"}]");
        when(revisionRepository.findById(7L)).thenReturn(Optional.of(old));

        ScriptPluginDTO dto = service.rollback("packing-wave", 7L);

        assertEquals(7L, dto.getCurrentRevisionId());
        verify(redisUtils).publish(any(), any(PluginManagementMessage.class));
    }

    @Test
    void getEnabledBundles_returnsBundlesOrderedByPluginKey() {
        ScriptPlugin b = plugin("b-plugin", "wes", ScriptPluginStatusEnum.ENABLED);
        b.setId(2L);
        ScriptPlugin a = plugin("a-plugin", "wes", ScriptPluginStatusEnum.ENABLED);
        when(pluginRepository.findByApplySystemAndStatusOrderByPluginKeyAsc("wes", ScriptPluginStatusEnum.ENABLED))
                .thenReturn(List.of(a, b));
        ScriptPluginRevision revision = revision();
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision));
        when(configRepository.findByPluginId(any())).thenReturn(Optional.empty());

        List<ScriptPluginBundleDTO> bundles = service.getEnabledBundles("wes");

        assertEquals(List.of("a-plugin", "b-plugin"), bundles.stream().map(ScriptPluginBundleDTO::getPluginKey).toList());
    }
}
