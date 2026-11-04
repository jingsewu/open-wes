package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;

import java.util.List;

@Data
public class ScriptPluginDTO {
    private Long id;
    @NotEmpty
    @Size(max = 64)
    private String pluginKey;
    @NotEmpty
    @Size(max = 128)
    private String name;
    @Size(max = 512)
    private String description;
    @NotEmpty
    @Size(max = 32)
    private String applySystem;
    @Size(max = 64)
    private String applyModule;
    private ScriptPluginStatusEnum status;
    private List<ExtensionDeclarationDTO> extensionPoints;
    private List<ConfigFieldDTO> configSchema;
    private String sdkVersion;
    private Long currentRevisionId;
    private String lastValidationStatus;
    private String lastValidationError;
    private Long lastValidationAt;
    private Long updateTime;
}
