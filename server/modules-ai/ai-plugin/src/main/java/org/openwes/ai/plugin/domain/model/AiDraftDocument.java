package org.openwes.ai.plugin.domain.model;

import lombok.Data;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The model's structured output; field names mirror the platform DTOs exactly. */
@Data
public class AiDraftDocument {

    private Meta meta = new Meta();
    private List<ExtensionDeclarationDTO> extensions = new ArrayList<>();
    private List<ConfigFieldDTO> configSchema = new ArrayList<>();
    private Map<String, String> scripts = new LinkedHashMap<>();

    @Data
    public static class Meta {
        private String name;
        private String changeSummary;
    }
}
