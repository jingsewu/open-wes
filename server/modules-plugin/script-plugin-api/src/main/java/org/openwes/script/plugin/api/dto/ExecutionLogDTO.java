package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.ExtensionType;

@Data
public class ExecutionLogDTO {
    private String pluginKey;
    private Long revisionId;
    private ExtensionType extensionType;
    private boolean success;
    private long durationMs;
    private String errorMessage;
    private Long createdAt;
}
