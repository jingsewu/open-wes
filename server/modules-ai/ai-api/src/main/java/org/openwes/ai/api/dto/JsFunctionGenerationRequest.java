package org.openwes.ai.api.dto;

import lombok.Data;

@Data
public class JsFunctionGenerationRequest {
    private String inputFormat;
    private String outputFormat;
    private String transformationRules;
}
