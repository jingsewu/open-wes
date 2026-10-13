package org.openwes.script.plugin.api.dto;

import lombok.Data;

/** One field of the plugin's configuration schema; read inside scripts via ctx.config* . */
@Data
public class ConfigFieldDTO {
    private String field;
    private String type;
    private boolean required;
    private String defaultValue;
}
