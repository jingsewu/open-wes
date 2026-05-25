package org.openwes.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {
    private int tickIntervalMs = 200;
    private double defaultRobotSpeed = 2.0;
    private int loadingDelayMs = 1500;
    private int maxRobots = 20;
    private int failureRatePercent = 0;
    private String layoutFile = "classpath:layouts/default-layout.json";
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:8092,http://3d-viewer:8092";
    }
}
