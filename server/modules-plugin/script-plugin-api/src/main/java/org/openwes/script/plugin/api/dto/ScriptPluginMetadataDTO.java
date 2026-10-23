package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScriptPluginMetadataDTO {
    @NotBlank
    @Size(max = 128)
    private String name;
    @Size(max = 512)
    private String description;
    @Size(max = 64)
    private String applyModule;
}
