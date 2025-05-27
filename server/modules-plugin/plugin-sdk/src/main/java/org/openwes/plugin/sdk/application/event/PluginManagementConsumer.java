package org.openwes.plugin.sdk.application.event;

import org.openwes.common.utils.tenant.TenantContext;
import org.openwes.mq.redis.RedisListener;
import org.openwes.plugin.api.constants.PluginManageTypeEnum;
import org.openwes.plugin.api.constants.TopicConstants;
import org.openwes.plugin.api.dto.PluginManageDTO;
import org.openwes.plugin.sdk.application.PluginApiImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PluginManagementConsumer {

    private final PluginApiImpl pluginApi;

    @RedisListener(topic = TopicConstants.TOPIC_PLUGIN_LISTEN_PLUGIN_MODIFY, type = PluginManageDTO.class)
    public void listPluginManage(String topic, PluginManageDTO pluginManageDTO) throws IOException {
        if (pluginManageDTO == null) {
            return;
        }

        try {
            TenantContext.setCurrentTenant(pluginManageDTO.getTenantName());

            if (Objects.requireNonNull(pluginManageDTO.getPluginManageType()) == PluginManageTypeEnum.INSTALL) {
                pluginApi.install(pluginManageDTO);
            }
        } finally {
            TenantContext.removeTenant();
        }

    }
}
