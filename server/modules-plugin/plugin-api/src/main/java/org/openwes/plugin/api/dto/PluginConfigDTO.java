package org.openwes.plugin.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PluginConfigDTO implements Serializable {

    @NotEmpty
    private String pluginUniqueKey;

    @NotEmpty
    private String configInfo;
}
