package org.openwes.plugin.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PluginConfigDTO implements Serializable {

    private Long id;

    @NotEmpty
    private String pluginUniqueKey;

    private String tenantName;

    @NotEmpty
    private String configInfo;

    private Long version;
}
