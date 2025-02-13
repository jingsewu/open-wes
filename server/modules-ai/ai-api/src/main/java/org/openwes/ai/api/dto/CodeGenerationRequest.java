package org.openwes.ai.api.dto;

import lombok.Data;

@Data
public class CodeGenerationRequest {
    private String codeContext;
    private String lineContent;
    private String language;
}
