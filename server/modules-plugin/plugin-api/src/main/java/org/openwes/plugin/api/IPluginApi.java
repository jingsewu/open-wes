package org.openwes.plugin.api;

import org.openwes.plugin.api.dto.PluginConfigDTO;

public interface IPluginApi {
    PluginConfigDTO getPluginConfig(String pluginUniqueKey);
}
