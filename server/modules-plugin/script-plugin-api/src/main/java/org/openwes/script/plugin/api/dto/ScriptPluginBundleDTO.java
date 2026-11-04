package org.openwes.script.plugin.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Everything a target node needs to materialize one plugin revision: identity, declarations,
 * script sources and the current configuration values.
 */
@Data
public class ScriptPluginBundleDTO implements Serializable {
    private String pluginKey;
    private String name;
    private String applySystem;
    private String applyModule;
    private String sdkVersion;
    private Long revisionId;
    private List<ExtensionDeclarationDTO> declarations;
    private List<ConfigFieldDTO> configSchema;
    private Map<String, String> scripts;
    /** config field -> value, resolved from p_script_plugin_config and schema defaults */
    private Map<String, Object> config;
}
