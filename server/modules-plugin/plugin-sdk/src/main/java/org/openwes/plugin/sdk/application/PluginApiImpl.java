package org.openwes.plugin.sdk.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.api.IPluginApi;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.openwes.plugin.sdk.domain.repository.ApplicationPluginConfigRepository;
import org.openwes.plugin.sdk.domain.service.PluginService;
import org.openwes.plugin.sdk.domain.transfer.ApplicationPluginConfigTransfer;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PluginApiImpl implements IPluginApi {

    private final ApplicationPluginConfigTransfer pluginConfigTransfer;
    private final PluginService pluginService;

    @Override
    public PluginConfigDTO getPluginConfig(String pluginUniqueKey) {
        return pluginConfigTransfer.toDTO(pluginService.get(pluginUniqueKey));
    }

}
