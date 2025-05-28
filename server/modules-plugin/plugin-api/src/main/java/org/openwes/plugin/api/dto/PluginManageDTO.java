package org.openwes.plugin.api.dto;

import org.openwes.plugin.api.constants.PluginStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PluginManageDTO {

    private PluginStatusEnum pluginManageType;

    private String tenantName;
    private Long pluginId;
    private String pluginUniqueKey;
    private String pluginJarPath;
    private String pluginConfigPath;
    private String version;
}
