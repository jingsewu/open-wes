package org.openwes.ai.plugin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiGenerationDTO {
    private Long id;
    private String pluginKey;
    private String userName;
    private String model;
    private String promptVersion;
    private String status;
    private int attempts;
    private Integer promptTokens;
    private Integer completionTokens;
    private Long durationMs;
    private String errorMessage;
    private Long createdAt;
    private Long finishedAt;
}
