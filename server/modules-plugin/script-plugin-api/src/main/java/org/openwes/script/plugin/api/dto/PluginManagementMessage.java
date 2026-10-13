package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.PluginManagementMessageType;

/** Payload of the Redis PLUGIN_LISTEN_PLUGIN_MANAGEMENT channel (Redisson JsonJacksonCodec). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginManagementMessage {
    private String pluginKey;
    private PluginManagementMessageType type;
}
