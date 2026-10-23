package org.openwes.script.plugin.core.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginContentHasher;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.*;
import org.openwes.script.plugin.api.enums.PluginManagementMessageType;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginConfig;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginConfigRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRevisionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginService {

    private final ScriptPluginRepository pluginRepository;
    private final ScriptPluginRevisionRepository revisionRepository;
    private final ScriptPluginConfigRepository configRepository;
    private final List<ScriptPluginMaterializationValidator> materializationValidators;
    private final RedisUtils redisUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${script-plugin.validation-timeout-ms:30000}")
    private long validationTimeoutMs = 30000;

    private record PendingValidation(String pluginKey, Long revisionId, String app,
                                     ScriptPluginBundleDTO bundle,
                                     CompletableFuture<ValidationReportDTO> future) {
    }

    private final Map<String, PendingValidation> pendingValidations = new ConcurrentHashMap<>();

    public ScriptPluginDTO createDraft(ScriptPluginDTO dto) {
        if (StringUtils.isBlank(dto.getPluginKey()) || StringUtils.isBlank(dto.getName())
                || StringUtils.isBlank(dto.getApplySystem())) {
            throw new CommonException("pluginKey, name and applySystem are required");
        }
        if (!dto.getPluginKey().matches("[A-Za-z0-9._-]+")) {
            throw new CommonException("pluginKey may only contain letters, digits, '.', '_' and '-'");
        }
        if (!ScriptPluginApiConstants.SUPPORTED_APPS.contains(dto.getApplySystem())) {
            throw new CommonException("unsupported applySystem: " + dto.getApplySystem());
        }
        pluginRepository.findByPluginKey(dto.getPluginKey()).ifPresent(existing -> {
            throw new CommonException("script plugin already exists: " + dto.getPluginKey());
        });

        ScriptPlugin plugin = new ScriptPlugin();
        plugin.setPluginKey(dto.getPluginKey());
        plugin.setName(dto.getName());
        plugin.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        plugin.setApplySystem(dto.getApplySystem());
        plugin.setApplyModule(dto.getApplyModule() == null ? "" : dto.getApplyModule());
        plugin.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        plugin.setExtensionPoints("[]");
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        return toDto(pluginRepository.save(plugin));
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto) {
        return saveRevision(pluginKey, dto, RevisionOriginEnum.MANUAL, null, null);
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto,
                                                RevisionOriginEnum origin, String aiModel, String promptVersion) {
        return saveRevision(pluginKey, dto, origin, aiModel, promptVersion,
                UserContext.getCurrentUser());
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto,
                                                RevisionOriginEnum origin, String aiModel, String promptVersion,
                                                String actor) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        if (plugin.getStatus() == ScriptPluginStatusEnum.ENABLED) {
            throw new CommonException("disable the plugin before saving a new revision");
        }
        if (dto.getScripts() == null || dto.getScripts().isEmpty()) {
            throw new CommonException("a revision needs at least one script");
        }
        List<ExtensionDeclarationDTO> declarations = dto.getExtensionPoints() == null
                ? List.of() : dto.getExtensionPoints();

        int revisionNo = revisionRepository.findTopByPluginIdOrderByRevisionNoDesc(plugin.getId())
                .map(r -> r.getRevisionNo() + 1).orElse(1);

        ScriptPluginRevision revision = new ScriptPluginRevision();
        revision.setPluginId(plugin.getId());
        revision.setRevisionNo(revisionNo);
        revision.setScripts(writeJson(dto.getScripts()));
        revision.setExtensionPointsSnapshot(writeJson(declarations));
        revision.setConfigSchemaSnapshot(dto.getConfigSchema() == null ? null : writeJson(dto.getConfigSchema()));
        revision.setContentHash(ScriptPluginContentHasher.hash(declarations, dto.getScripts()));
        revision.setChangeSummary(dto.getChangeSummary() == null ? "" : dto.getChangeSummary());
        revision.setOrigin(origin);
        revision.setAiModel(aiModel);
        revision.setPromptVersion(promptVersion);
        revision.setCreatedBy(StringUtils.left(
                StringUtils.defaultIfBlank(actor, AuthConstants.USERNAME_SYSTEM), 60));
        revision.setCreatedAt(System.currentTimeMillis());
        revision = revisionRepository.save(revision);

        plugin.setCurrentRevisionId(revision.getId());
        plugin.setExtensionPoints(revision.getExtensionPointsSnapshot());
        plugin.setConfigSchema(revision.getConfigSchemaSnapshot());
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);

        return toRevisionDto(revision);
    }

    public List<ScriptPluginDTO> list() {
        return pluginRepository.findAll().stream().map(this::toDto).toList();
    }

    public ScriptPluginDTO detail(String pluginKey) {
        return toDto(require(pluginKey));
    }

    public List<ScriptPluginRevisionDTO> revisions(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        return revisionRepository.findAllByPluginIdOrderByRevisionNoDesc(plugin.getId())
                .stream().map(this::toRevisionDto).toList();
    }

    /**
     * Enable-time validation (spec §Enable-time validation): runs on the node whose app matches the
     * plugin. The hosting node (wes) validates in-process; remote apps are asked through the
     * pending-validation channel and must report back within the timeout.
     */
    public ValidationReportDTO validate(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        ScriptPluginBundleDTO bundle = getBundle(pluginKey);
        if (bundle == null) {
            throw new CommonException("plugin has no revision to validate: " + pluginKey);
        }

        Optional<ScriptPluginMaterializationValidator> localValidator = materializationValidators.stream()
                .filter(v -> v.targetApp().equals(plugin.getApplySystem())).findFirst();
        if (localValidator.isPresent()) {
            return localValidator.get().validate(bundle);
        }

        Long revisionId = bundle.getRevisionId();
        String key = pendingValidationKey(pluginKey, revisionId);
        PendingValidation candidate = new PendingValidation(pluginKey, revisionId, plugin.getApplySystem(),
                bundle, new CompletableFuture<>());
        PendingValidation existing = pendingValidations.putIfAbsent(key, candidate);
        boolean firstCaller = existing == null;
        PendingValidation pending = firstCaller ? candidate : existing;

        try {
            if (firstCaller) {
                publishManagementMessage(plugin);
            }
            return pending.future().get(validationTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("validation for plugin {} on app {} did not report within {} ms",
                    pluginKey, plugin.getApplySystem(), validationTimeoutMs);
            return ValidationReportDTO.failed(pluginKey, revisionId, plugin.getApplySystem(),
                    "remote", "target node " + plugin.getApplySystem() + " did not report within "
                            + validationTimeoutMs + " ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonException("validation interrupted");
        } catch (ExecutionException e) {
            throw new CommonException("validation failed: " + e.getCause().getMessage());
        } finally {
            pendingValidations.remove(key, pending);
        }
    }

    private static String pendingValidationKey(String pluginKey, Long revisionId) {
        return pluginKey + "#" + revisionId;
    }

    public ScriptPluginDTO enable(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() != ScriptPluginStatusEnum.DRAFT && plugin.getStatus() != ScriptPluginStatusEnum.DISABLED) {
            throw new CommonException("only DRAFT or DISABLED plugins can be enabled");
        }
        if (plugin.getCurrentRevisionId() == null) {
            throw new CommonException("plugin has no revision to enable");
        }
        if (!ScriptPluginApiConstants.CONTRACT_VERSION.equals(plugin.getSdkVersion())) {
            throw new CommonException("plugin sdk_version " + plugin.getSdkVersion()
                    + " does not equal platform contract " + ScriptPluginApiConstants.CONTRACT_VERSION);
        }

        ValidationReportDTO report = validate(pluginKey);
        plugin.setLastValidationStatus(report.isPassed() ? "PASSED" : "FAILED");
        plugin.setLastValidationError(report.isPassed() ? null : report.failureSummary());
        plugin.setLastValidationAt(report.getFinishedAt());
        if (!report.isPassed()) {
            pluginRepository.save(plugin);
            throw new CommonException("enable refused, validation failed:\n" + plugin.getLastValidationError());
        }

        plugin.enable();
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);
        return toDto(plugin);
    }

    public ScriptPluginDTO disable(String pluginKey, String reason) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ENABLED) {
            plugin.disable();
            pluginRepository.save(plugin);
            publishManagementMessage(plugin);
            log.warn("script plugin {} disabled: {}", pluginKey, reason);
        }
        return toDto(plugin);
    }

    public ScriptPluginDTO archive(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        plugin.archive();
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);
        return toDto(plugin);
    }

    public ScriptPluginDTO rollback(String pluginKey, long revisionId) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins cannot be rolled back");
        }
        ScriptPluginRevision revision = revisionRepository.findById(revisionId)
                .filter(r -> r.getPluginId().equals(plugin.getId()))
                .orElseThrow(() -> new CommonException("revision not found: " + revisionId));

        plugin.setCurrentRevisionId(revision.getId());
        plugin.setExtensionPoints(revision.getExtensionPointsSnapshot());
        plugin.setConfigSchema(revision.getConfigSchemaSnapshot());
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);
        return toDto(plugin);
    }

    public ScriptPluginDTO updateMetadata(String pluginKey, String name, String description, String applyModule) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        if (StringUtils.isBlank(name)) {
            throw new CommonException("name is required");
        }
        plugin.updateMetadata(name, description, applyModule);
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);
        return toDto(plugin);
    }

    public ScriptPluginConfigDTO getConfig(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO();
        dto.setValues(resolveConfig(plugin));
        dto.setSchema(readConfigSchema(plugin));
        dto.setLockVersion(configRepository.findByPluginId(plugin.getId())
                .map(ScriptPluginConfig::getLockVersion).orElse(null));
        return dto;
    }

    public ScriptPluginConfigDTO saveConfig(String pluginKey, Map<String, Object> values, Long lockVersion) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        Map<String, Object> safeValues = values == null ? Map.of() : values;
        ScriptPluginConfigValidator.validate(readConfigSchema(plugin), safeValues);

        ScriptPluginConfig row = configRepository.findByPluginId(plugin.getId()).orElse(null);
        if (row == null) {
            if (lockVersion != null) {
                throw new CommonException(
                        "config was modified by another user; current lockVersion is null");
            }
            row = new ScriptPluginConfig();
            row.setPluginId(plugin.getId());
        } else if (!Objects.equals(row.getLockVersion(), lockVersion)) {
            throw new CommonException("config was modified by another user; current lockVersion is "
                    + row.getLockVersion());
        }
        row.setConfigJson(writeJson(safeValues));
        configRepository.save(row);
        publishManagementMessage(plugin);
        return getConfig(pluginKey);
    }

    private List<ConfigFieldDTO> readConfigSchema(ScriptPlugin plugin) {
        if (plugin.getConfigSchema() == null) {
            return List.of();
        }
        return readJson(plugin.getConfigSchema(), new TypeReference<>() {
        });
    }

    public ScriptPluginBundleDTO getBundle(String pluginKey) {
        return pluginRepository.findByPluginKey(pluginKey).map(this::toBundle).orElse(null);
    }

    public List<ScriptPluginBundleDTO> getEnabledBundles(String applySystem) {
        return pluginRepository.findByApplySystemAndStatusOrderByPluginKeyAsc(applySystem, ScriptPluginStatusEnum.ENABLED)
                .stream().map(this::toBundle).filter(Objects::nonNull).toList();
    }

    private ScriptPluginBundleDTO toBundle(ScriptPlugin plugin) {
        if (plugin.getCurrentRevisionId() == null) {
            return null;
        }
        ScriptPluginRevision revision = revisionRepository.findById(plugin.getCurrentRevisionId()).orElse(null);
        if (revision == null) {
            return null;
        }

        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey(plugin.getPluginKey());
        bundle.setName(plugin.getName());
        bundle.setApplySystem(plugin.getApplySystem());
        bundle.setApplyModule(plugin.getApplyModule());
        bundle.setSdkVersion(plugin.getSdkVersion());
        bundle.setRevisionId(revision.getId());
        bundle.setDeclarations(readDeclarations(revision.getExtensionPointsSnapshot()));
        bundle.setConfigSchema(revision.getConfigSchemaSnapshot() == null
                ? List.of() : readJson(revision.getConfigSchemaSnapshot(), new TypeReference<>() {
        }));
        bundle.setScripts(readJson(revision.getScripts(), new TypeReference<>() {
        }));
        bundle.setConfig(resolveConfig(plugin));
        return bundle;
    }

    public long getRegistryVersion(String applySystem) {
        return pluginRepository.findMaxUpdateTime(applySystem);
    }

    public ScriptPluginBundleDTO findPendingValidation(String applySystem) {
        return pendingValidations.values().stream()
                .filter(pending -> applySystem.equals(pending.app()))
                .map(PendingValidation::bundle)
                .findFirst().orElse(null);
    }

    public void reportValidation(ValidationReportDTO report) {
        PendingValidation pending = pendingValidations.remove(
                pendingValidationKey(report.getPluginKey(), report.getRevisionId()));
        if (pending != null) {
            pending.future().complete(report);
        } else {
            log.info("validation report for {} (revision {}) has no pending request, ignoring late report",
                    report.getPluginKey(), report.getRevisionId());
        }
        pluginRepository.findByPluginKey(report.getPluginKey()).ifPresent(plugin -> {
            if (Objects.equals(plugin.getCurrentRevisionId(), report.getRevisionId())) {
                plugin.setLastValidationStatus(report.isPassed() ? "PASSED" : "FAILED");
                plugin.setLastValidationError(report.isPassed() ? null : report.failureSummary());
                plugin.setLastValidationAt(report.getFinishedAt());
                pluginRepository.save(plugin);
            }
        });
        log.info("validation report for {} (revision {}): passed={}", report.getPluginKey(),
                report.getRevisionId(), report.isPassed());
    }

    private ScriptPlugin require(String pluginKey) {
        return pluginRepository.findByPluginKey(pluginKey)
                .orElseThrow(() -> new CommonException("script plugin not found: " + pluginKey));
    }

    private void publishManagementMessage(ScriptPlugin plugin) {
        redisUtils.publish(RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT,
                new PluginManagementMessage(plugin.getPluginKey(), plugin.getCurrentRevisionId(),
                        PluginManagementMessageType.PLUGIN_CHANGED));
    }

    private Map<String, Object> resolveConfig(ScriptPlugin plugin) {
        Map<String, Object> config = new HashMap<>();
        configRepository.findByPluginId(plugin.getId()).ifPresent(row ->
                config.putAll(readJson(row.getConfigJson(), new TypeReference<>() {
                })));
        if (plugin.getConfigSchema() != null) {
            for (ConfigFieldDTO field : readJson(plugin.getConfigSchema(), new TypeReference<List<ConfigFieldDTO>>() {
            })) {
                if (!config.containsKey(field.getField()) && field.getDefaultValue() != null) {
                    config.put(field.getField(), field.getDefaultValue());
                }
            }
        }
        return config;
    }

    private List<ExtensionDeclarationDTO> readDeclarations(String json) {
        if (json == null) {
            return List.of();
        }
        return readJson(json, new TypeReference<>() {
        });
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new CommonException("cannot parse stored JSON: " + e.getMessage());
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new CommonException("cannot serialize JSON: " + e.getMessage());
        }
    }

    private ScriptPluginDTO toDto(ScriptPlugin plugin) {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setId(plugin.getId());
        dto.setPluginKey(plugin.getPluginKey());
        dto.setName(plugin.getName());
        dto.setDescription(plugin.getDescription());
        dto.setApplySystem(plugin.getApplySystem());
        dto.setApplyModule(plugin.getApplyModule());
        dto.setStatus(plugin.getStatus());
        dto.setExtensionPoints(readDeclarations(plugin.getExtensionPoints()));
        dto.setConfigSchema(plugin.getConfigSchema() == null
                ? List.of() : readJson(plugin.getConfigSchema(), new TypeReference<>() {
        }));
        dto.setSdkVersion(plugin.getSdkVersion());
        dto.setCurrentRevisionId(plugin.getCurrentRevisionId());
        dto.setLastValidationStatus(plugin.getLastValidationStatus());
        dto.setLastValidationError(plugin.getLastValidationError());
        dto.setLastValidationAt(plugin.getLastValidationAt());
        dto.setUpdateTime(plugin.getUpdateTime());
        return dto;
    }

    private ScriptPluginRevisionDTO toRevisionDto(ScriptPluginRevision revision) {
        ScriptPluginRevisionDTO dto = new ScriptPluginRevisionDTO();
        dto.setId(revision.getId());
        dto.setPluginId(revision.getPluginId());
        dto.setRevisionNo(revision.getRevisionNo());
        dto.setScripts(readJson(revision.getScripts(), new TypeReference<>() {
        }));
        dto.setExtensionPoints(readDeclarations(revision.getExtensionPointsSnapshot()));
        dto.setConfigSchema(revision.getConfigSchemaSnapshot() == null
                ? List.of() : readJson(revision.getConfigSchemaSnapshot(), new TypeReference<>() {
        }));
        dto.setContentHash(revision.getContentHash());
        dto.setChangeSummary(revision.getChangeSummary());
        dto.setOrigin(revision.getOrigin());
        dto.setAiModel(revision.getAiModel());
        dto.setPromptVersion(revision.getPromptVersion());
        dto.setCreatedBy(revision.getCreatedBy());
        dto.setCreatedAt(revision.getCreatedAt());
        return dto;
    }
}
