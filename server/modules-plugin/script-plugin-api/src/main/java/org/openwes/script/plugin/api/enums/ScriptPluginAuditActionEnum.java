package org.openwes.script.plugin.api.enums;

/**
 * Operation-audit vocabulary. Plain enum: these values are not dictionary-backed configuration.
 */
public enum ScriptPluginAuditActionEnum {
    CREATE_DRAFT,
    SAVE_REVISION,
    VALIDATE,
    ENABLE,
    DISABLE,
    ARCHIVE,
    ROLLBACK,
    CONFIG_SAVE,
    METADATA_UPDATE,
    TEST_RUN
}
