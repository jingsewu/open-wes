package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStatsDTO {

    private long total;
    private long failed;
    /** Percentage in [0, 100], rounded to two decimals. */
    private double failureRate;
    private long avgDurationMs;
    private long maxDurationMs;
    private List<TypeStats> byType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStats {
        private ExtensionType extensionType;
        private long total;
        private long failed;
        private long avgDurationMs;
        private long maxDurationMs;
    }
}
