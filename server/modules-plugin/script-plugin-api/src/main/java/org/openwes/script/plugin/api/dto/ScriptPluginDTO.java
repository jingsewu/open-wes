package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;

import java.util.List;

@Data
public class ScriptPluginDTO {
    private Long id;
    private String pluginKey;
    private String name;
    private String description;
    private String applySystem;
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
