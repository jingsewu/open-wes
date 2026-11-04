import { AuditAction, ExtensionType, PluginStatus, RevisionOrigin } from "./types"

export const STATUS_LABEL_KEYS: Record<PluginStatus, string> = {
    DRAFT: "pluginPlatform.status.draft",
    ENABLED: "pluginPlatform.status.enabled",
    DISABLED: "pluginPlatform.status.disabled",
    ARCHIVED: "pluginPlatform.status.archived"
}

export const STATUS_COLORS: Record<PluginStatus, string> = {
    DRAFT: "default",
    ENABLED: "green",
    DISABLED: "orange",
    ARCHIVED: "red"
}

export const VALIDATION_LABEL_KEYS: Record<string, string> = {
    PASSED: "pluginPlatform.validation.passed",
    FAILED: "pluginPlatform.validation.failed"
}

export const REVISION_ORIGIN_LABEL_KEYS: Record<RevisionOrigin, string> = {
    AI: "pluginPlatform.revisionOrigin.ai",
    MANUAL: "pluginPlatform.revisionOrigin.manual"
}

export const AI_STATUS_LABEL_KEYS: Record<string, string> = {
    RUNNING: "pluginPlatform.aiStatus.running",
    SUCCEEDED: "pluginPlatform.aiStatus.succeeded",
    FAILED: "pluginPlatform.aiStatus.failed",
    CANCELLED: "pluginPlatform.aiStatus.cancelled"
}

export const AI_STATUS_COLORS: Record<string, string> = {
    RUNNING: "blue",
    SUCCEEDED: "green",
    FAILED: "red",
    CANCELLED: "default"
}

export const EXTENSION_TYPE_LABEL_KEYS: Record<ExtensionType, string> = {
    OUTBOUND_WAVE_WAVE_ACTION: "pluginPlatform.extensionType.outboundWaveWaveAction",
    OUTBOUND_WAVE_SPLIT_ACTION: "pluginPlatform.extensionType.outboundWaveSplitAction",
    REQUEST_VALIDATE: "pluginPlatform.extensionType.requestValidate",
    REQUEST_SUPPLY: "pluginPlatform.extensionType.requestSupply",
    REQUEST_AFTER_INVOKE: "pluginPlatform.extensionType.requestAfterInvoke",
    CUSTOM_REQUEST_INVOKE: "pluginPlatform.extensionType.customRequestInvoke",
    CUSTOM_API: "pluginPlatform.extensionType.customApi",
    LIFECYCLE_LISTENER: "pluginPlatform.extensionType.lifecycleListener",
    HTTP_ENDPOINT: "pluginPlatform.extensionType.httpEndpoint",
    CRON: "pluginPlatform.extensionType.cron"
}

export const AUDIT_ACTION_LABEL_KEYS: Record<AuditAction, string> = {
    CREATE_DRAFT: "pluginPlatform.auditAction.createDraft",
    SAVE_REVISION: "pluginPlatform.auditAction.saveRevision",
    VALIDATE: "pluginPlatform.auditAction.validate",
    ENABLE: "pluginPlatform.auditAction.enable",
    DISABLE: "pluginPlatform.auditAction.disable",
    ARCHIVE: "pluginPlatform.auditAction.archive",
    ROLLBACK: "pluginPlatform.auditAction.rollback",
    CONFIG_SAVE: "pluginPlatform.auditAction.configSave",
    METADATA_UPDATE: "pluginPlatform.auditAction.metadataUpdate",
    TEST_RUN: "pluginPlatform.auditAction.testRun"
}
