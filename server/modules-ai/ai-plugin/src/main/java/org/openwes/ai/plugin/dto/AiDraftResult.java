package org.openwes.ai.plugin.dto;

import lombok.Builder;
import lombok.Data;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiDraftResult {
    private String pluginKey;
    private Long revisionId;
    private Integer revisionNo;
    private String changeSummary;
    private List<ExtensionDeclarationDTO> extensions;
    private List<ConfigFieldDTO> configSchema;
    private Map<String, String> scripts;
    private boolean materializationVerified;
    private int attempts;
    private String model;
    private String promptVersion;
}
