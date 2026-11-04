package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.api.enums.ExtensionType;

/** JPQL projection for stats grouped by extension type. */
public interface ExecutionTypeStatsView {
    ExtensionType getExtensionType();

    long getTotal();

    long getFailed();

    Double getAvgDurationMs();

    Long getMaxDurationMs();
}
