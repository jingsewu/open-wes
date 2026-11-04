package org.openwes.script.plugin.api.enums;

/**
 * The extensible archetypes. Each value maps to exactly one host extension interface and one
 * script method (see {@code PluginContractCatalog}).
 */
public enum ExtensionType {
    /** WES outbound wave grouping hook. */
    OUTBOUND_WAVE_WAVE_ACTION,
    /** WES outbound wave split-to-picking hook. */
    OUTBOUND_WAVE_SPLIT_ACTION,
    /** API platform request validation hook. */
    REQUEST_VALIDATE,
    /** API platform request enrichment hook. */
    REQUEST_SUPPLY,
    /** API platform post-invoke hook. */
    REQUEST_AFTER_INVOKE,
    /** API platform custom request plugin (replaces the built-in invoke). */
    CUSTOM_REQUEST_INVOKE,
    /** Station custom API executed from the workstation. */
    CUSTOM_API,
    /** Entity lifecycle listener (entityName declared per plugin). */
    LIFECYCLE_LISTENER,
    /** Self-provided HTTP endpoint under /plugin-platform/ext/{pluginKey}/**. */
    HTTP_ENDPOINT,
    /** Self-provided cron job. */
    CRON
}
