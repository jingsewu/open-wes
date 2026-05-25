package org.openwes.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wes")
public class WesProperties {
    private String callbackUrl;
    private Api api = new Api();

    @Data
    public static class Api {
        private String containerArrive;
        private String containerLeave;
        private String taskStatusUpdate;
    }
}
