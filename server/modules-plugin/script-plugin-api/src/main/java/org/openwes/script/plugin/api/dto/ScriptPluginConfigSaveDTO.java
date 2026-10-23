package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ScriptPluginConfigSaveDTO {
    @NotNull
    private Map<String, Object> values;
    private Long lockVersion;
}
