package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.PluginManagementMessage;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.PluginManagementMessageType;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginConfigRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRevisionRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
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

    private ScriptPluginRevisionDTO revisionDto() {
        ScriptPluginRevisionDTO dto = new ScriptPluginRevisionDTO();
        dto.setScripts(Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"));
        dto.setExtensionPoints(List.of());
        return dto;
    }

    private ScriptPluginDTO draftDto(String pluginKey) {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey(pluginKey);
        dto.setName("Packing wave rule");
        dto.setApplySystem("wes");
        return dto;
    }

    @Test
    void saveRevision_onEnabledPlugin_isRejectedWithoutWritingOrPublishing() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.ENABLED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        CommonException e = assertThrows(CommonException.class,
                () -> service.saveRevision("packing-wave", revisionDto()));

        assertEquals("disable the plugin before saving a new revision", e.getMessage());
        verify(revisionRepository, never()).save(any());
        verify(pluginRepository, never()).save(any());
        verify(redisUtils, never()).publish(any(), any());
    }

    @Test
    void saveRevision_onDisabledPlugin_isAllowed() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DISABLED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findTopByPluginIdOrderByRevisionNoDesc(1L)).thenReturn(Optional.empty());
        when(revisionRepository.save(any())).thenAnswer(invocation -> {
            ScriptPluginRevision revision = invocation.getArgument(0);
            revision.setId(12L);
            return revision;
        });
        when(pluginRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto());

        assertEquals(12L, saved.getId());
        assertEquals(12L, plugin.getCurrentRevisionId());
        verify(revisionRepository).save(any());
        verify(pluginRepository).save(plugin);
        verify(redisUtils).publish(any(), any());
    }

    private void stubRevisionSave() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DISABLED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findTopByPluginIdOrderByRevisionNoDesc(1L)).thenReturn(Optional.empty());
        when(revisionRepository.save(any())).thenAnswer(invocation -> {
            ScriptPluginRevision revision = invocation.getArgument(0);
            revision.setId(12L);
            return revision;
        });
        when(pluginRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void saveRevision_manualSave_recordsTheCurrentUser() {
        UserContext.setAccount("bob");
        stubRevisionSave();

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto());

        assertEquals("bob", saved.getCreatedBy());
    }

    @Test
    void saveRevision_explicitActor_overridesTheContext() {
        UserContext.setAccount("bob");
        stubRevisionSave();

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto(),
                RevisionOriginEnum.MANUAL, null, null, "alice");

        assertEquals("alice", saved.getCreatedBy());
    }

    @Test
    void saveRevision_blankActor_recordsSystem() {
        stubRevisionSave();

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto(),
                RevisionOriginEnum.MANUAL, null, null, " ");

        assertEquals("system", saved.getCreatedBy());
    }

    @Test
    void createDraft_withInvalidPluginKeyCharacters_isRejected() {
        for (String pluginKey : List.of("packing#wave", "packing/wave")) {
            CommonException e = assertThrows(CommonException.class,
                    () -> service.createDraft(draftDto(pluginKey)));

            assertEquals("pluginKey may only contain letters, digits, '.', '_' and '-'", e.getMessage());
        }
        verify(pluginRepository, never()).save(any());
    }

    @Test
    void createDraft_withDashedPluginKey_isAccepted() {
        when(pluginRepository.findByPluginKey("packing-wave.v1_x")).thenReturn(Optional.empty());
        when(pluginRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginDTO created = service.createDraft(draftDto("packing-wave.v1_x"));

        assertEquals("packing-wave.v1_x", created.getPluginKey());
        assertEquals(ScriptPluginStatusEnum.DRAFT, created.getStatus());
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
        ArgumentCaptor<PluginManagementMessage> messageCaptor = ArgumentCaptor.forClass(PluginManagementMessage.class);
        verify(redisUtils).publish(eq(org.openwes.common.utils.constants.RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT),
                messageCaptor.capture());
        assertEquals("packing-wave", messageCaptor.getValue().getPluginKey());
        assertEquals(11L, messageCaptor.getValue().getRevisionId());
        assertEquals(PluginManagementMessageType.PLUGIN_CHANGED, messageCaptor.getValue().getType());
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

    private void awaitPendingValidation(String applySystem) throws InterruptedException {
        for (int i = 0; i < 200 && service.findPendingValidation(applySystem) == null; i++) {
            Thread.sleep(10);
        }
        assertNotNull(service.findPendingValidation(applySystem),
                "validation request must be pending for the " + applySystem + " poller");
    }

    private void awaitNoPendingValidation(String applySystem) throws InterruptedException {
        for (int i = 0; i < 100 && service.findPendingValidation(applySystem) != null; i++) {
            Thread.sleep(10);
        }
        assertNull(service.findPendingValidation(applySystem),
                "pending validation must be removed for the " + applySystem + " poller");
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
            awaitPendingValidation("station");

            service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

            assertTrue(result.get(2, TimeUnit.SECONDS).isPassed());
            awaitNoPendingValidation("station");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reportValidation_withWrongRevision_doesNotCompleteWaiter() throws Exception {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "validationTimeoutMs", 3000L);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ValidationReportDTO> result = executor.submit(() -> service.validate("station-api"));
            awaitPendingValidation("station");

            service.reportValidation(ValidationReportDTO.passed("station-api", 99L, "station"));

            for (int i = 0; i < 20 && !result.isDone(); i++) {
                Thread.sleep(10);
            }
            assertFalse(result.isDone(), "a report for another revision must not complete the waiter");

            service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

            assertTrue(result.get(2, TimeUnit.SECONDS).isPassed());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void validate_samePluginAndRevision_concurrentCallersShareOnePendingRequest() throws Exception {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "validationTimeoutMs", 3000L);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            Callable<ValidationReportDTO> validation = () -> {
                start.await();
                return service.validate("station-api");
            };
            Future<ValidationReportDTO> first = executor.submit(validation);
            Future<ValidationReportDTO> second = executor.submit(validation);

            start.countDown();
            awaitPendingValidation("station");
            Thread.sleep(100);
            service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

            ValidationReportDTO firstReport = first.get(2, TimeUnit.SECONDS);
            ValidationReportDTO secondReport = second.get(2, TimeUnit.SECONDS);
            assertTrue(firstReport.isPassed());
            assertTrue(secondReport.isPassed());
            assertSame(firstReport, secondReport, "both callers must receive the shared report");
            verify(redisUtils, times(1)).publish(any(), any());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void validate_publishFailure_removesThePendingEntry() throws Exception {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        doThrow(new IllegalStateException("redis unavailable")).when(redisUtils).publish(any(), any());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> service.validate("station-api"));

        assertEquals("redis unavailable", e.getMessage());
        assertNull(service.findPendingValidation("station"),
                "a failed publish must not leak the pending request");
    }

    @Test
    void reportValidation_staleRevision_doesNotOverwritePluginStatus() {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));

        service.reportValidation(ValidationReportDTO.failed("station-api", 99L, "station", "compile", "boom"));

        assertNull(plugin.getLastValidationStatus());
        assertNull(plugin.getLastValidationError());
        verify(pluginRepository, never()).save(any());
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
        ArgumentCaptor<PluginManagementMessage> messageCaptor = ArgumentCaptor.forClass(PluginManagementMessage.class);
        verify(redisUtils).publish(any(), messageCaptor.capture());
        assertEquals(7L, messageCaptor.getValue().getRevisionId());
    }

    @Test
    void updateMetadata_changesFieldsAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(pluginRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginDTO dto = service.updateMetadata("packing-wave", "New name", "desc", "outbound");

        assertEquals("New name", dto.getName());
        assertEquals("desc", dto.getDescription());
        assertEquals("outbound", dto.getApplyModule());
        verify(pluginRepository).save(plugin);
        verify(redisUtils).publish(any(), any());
    }

    @Test
    void updateMetadata_onArchivedPlugin_isRejectedWithoutWriting() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.ARCHIVED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        CommonException e = assertThrows(CommonException.class,
                () -> service.updateMetadata("packing-wave", "New name", null, null));

        assertEquals("archived plugins are read-only", e.getMessage());
        verify(pluginRepository, never()).save(any());
        verify(redisUtils, never()).publish(any(), any());
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
