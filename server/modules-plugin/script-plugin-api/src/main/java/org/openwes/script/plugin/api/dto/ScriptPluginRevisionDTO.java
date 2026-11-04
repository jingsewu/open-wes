package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;

import java.util.List;
import java.util.Map;

@Data
public class ScriptPluginRevisionDTO {
    private Long id;
    private Long pluginId;
    private Integer revisionNo;
    /** script file name -> groovy source */
    private Map<String, String> scripts;
    private List<ExtensionDeclarationDTO> extensionPoints;
    private List<ConfigFieldDTO> configSchema;
    private String contentHash;
    @Size(max = 512)
    private String changeSummary;
    private RevisionOriginEnum origin;
    @Size(max = 64)
    private String aiModel;
    @Size(max = 32)
    private String promptVersion;
    private String createdBy;
    private Long createdAt;
}
