package org.openwes.plugin.example;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.openwes.plugin.extension.config.ConfigParam;
import org.openwes.plugin.extension.config.IExtensionConfig;
import org.openwes.plugin.extension.config.InputType;

@AllArgsConstructor
@NoArgsConstructor
public class LazyOutboundWaveConfig implements IExtensionConfig {

    @ConfigParam(
            label = "API Key",
            description = "Your service API key",
            type = InputType.PASSWORD,
            required = true
    )
    private String apiKey;

    @ConfigParam(
            label = "Refresh Interval",
            description = "Data refresh interval in minutes",
            type = InputType.NUMBER,
            min = 1,
            max = 60
    )
    private int refreshInterval;
}
