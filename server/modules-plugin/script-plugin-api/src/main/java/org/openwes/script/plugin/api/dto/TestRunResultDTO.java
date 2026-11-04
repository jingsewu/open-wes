package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRunResultDTO {
    private boolean success;
    /** JSON text, truncated to 8 KB. */
    private String result;
    private boolean resultTruncated;
    private String error;
    private long durationMs;
}
