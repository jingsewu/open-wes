package org.openwes.script.plugin.api.dto;

import lombok.Data;

import java.io.Serializable;

/** One field of the plugin's configuration schema; read inside scripts via ctx.config* . */
@Data
public class ConfigFieldDTO implements Serializable {
    private String field;
    private String type;
    private boolean required;
    private String defaultValue;
}
