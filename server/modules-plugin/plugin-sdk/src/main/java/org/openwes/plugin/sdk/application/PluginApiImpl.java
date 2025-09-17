package org.openwes.plugin.sdk.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.api.IPluginApi;
import org.openwes.plugin.sdk.service.PluginService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PluginApiImpl implements IPluginApi {

    private final PluginService pluginService;

    @Override
    public String getPluginConfig(String pluginUniqueKey) {
        return pluginService.get(pluginUniqueKey);
    }

}
