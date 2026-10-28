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
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.PluginManagementMessageType;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginConfig;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginConfigRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRevisionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import static org.junit.jupiter.api.Assertions.fail;
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
        org.springframework.test.util.ReflectionTestUtils.setField(service, "validationTimeoutMs", 30_000L);

        CountDownLatch start = new CountDownLatch(1);
        List<ValidationReportDTO> reports = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        Runnable validation = () -> {
            try {
                start.await();
                reports.add(service.validate("station-api"));
            } catch (Throwable e) {
                failures.add(e);
            }
        };
        Thread first = new Thread(validation, "phase5-validation-1");
        Thread second = new Thread(validation, "phase5-validation-2");
        first.start();
        second.start();
        start.countDown();

        awaitPendingValidation("station");
        awaitBothCallersWaitingOnPendingValidation(first, second);
        service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

        first.join(5_000L);
        second.join(5_000L);
        assertFalse(first.isAlive(), "first caller must return");
        assertFalse(second.isAlive(), "second caller must return");
        assertTrue(failures.isEmpty(), () -> "validation failed: " + failures);
        assertEquals(2, reports.size());
        assertTrue(reports.get(0).isPassed());
        assertTrue(reports.get(1).isPassed());
        assertSame(reports.get(0), reports.get(1), "both callers must receive the shared report");
        verify(redisUtils, times(1)).publish(any(), any());
    }

    private void awaitBothCallersWaitingOnPendingValidation(Thread... threads) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            boolean allWaiting = true;
            for (Thread thread : threads) {
                allWaiting &= thread.getState() == Thread.State.TIMED_WAITING;
            }
            if (allWaiting) {
                return;
            }
            Thread.sleep(10);
        }
        fail("both validation callers must be waiting on the shared pending validation");
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
    void getConfig_mergesSchemaDefaultsAndReturnsLockVersion() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true,\"defaultValue\":\"20\"}]");
        ScriptPluginConfig row = new ScriptPluginConfig();
        row.setPluginId(1L);
        row.setConfigJson("{\"maxPerWave\":25}");
        row.setLockVersion(3L);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.of(row));

        ScriptPluginConfigDTO dto = service.getConfig("packing-wave");

        assertEquals(25, dto.getValues().get("maxPerWave"));
        assertEquals("maxPerWave", dto.getSchema().get(0).getField());
        assertEquals(3L, dto.getLockVersion());
    }

    @Test
    void saveConfig_withMatchingLockVersion_updatesAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true}]");
        ScriptPluginConfig row = new ScriptPluginConfig();
        row.setPluginId(1L);
        row.setConfigJson("{\"maxPerWave\":20}");
        row.setLockVersion(3L);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.of(row));
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginConfigDTO saved = service.saveConfig("packing-wave", Map.of("maxPerWave", 30), 3L);

        assertEquals(30, saved.getValues().get("maxPerWave"));
        verify(configRepository).save(row);
        verify(redisUtils).publish(any(), any());
    }

    @Test
    void saveConfig_withStaleLockVersion_isRejectedWithoutWriting() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true}]");
        ScriptPluginConfig row = new ScriptPluginConfig();
        row.setPluginId(1L);
        row.setConfigJson("{\"maxPerWave\":20}");
        row.setLockVersion(4L);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.of(row));

        CommonException e = assertThrows(CommonException.class,
                () -> service.saveConfig("packing-wave", Map.of("maxPerWave", 30), 3L));

        assertEquals("config was modified by another user; current lockVersion is 4", e.getMessage());
        verify(configRepository, never()).save(any());
        verify(redisUtils, never()).publish(any(), any());
    }

    @Test
    void saveConfig_withUnknownField_isRejected() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[]");
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        assertThrows(CommonException.class,
                () -> service.saveConfig("packing-wave", Map.of("unknown", 1), null));
        verify(configRepository, never()).save(any());
    }

    @Test
    void saveConfig_whenAbsent_insertsARowWithThePluginId() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true}]");
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveConfig("packing-wave", Map.of("maxPerWave", 30), null);

        ArgumentCaptor<ScriptPluginConfig> captor = ArgumentCaptor.forClass(ScriptPluginConfig.class);
        verify(configRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getPluginId());
        assertTrue(captor.getValue().getConfigJson().contains("30"));
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
