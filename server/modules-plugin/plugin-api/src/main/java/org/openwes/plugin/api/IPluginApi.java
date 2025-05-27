package org.openwes.plugin.api;

import org.openwes.plugin.api.dto.TenantPluginConfigDTO;

import java.util.List;

public interface IPluginApi {

    List<String> getStartedTenantPluginIds();

    TenantPluginConfigDTO getPluginConfig(String pluginUniqueKey);

    TenantPluginConfigDTO getPluginConfig(String tenantId, String pluginUniqueKey);

}
