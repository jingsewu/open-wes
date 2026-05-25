package org.openwes.simulator.config;

import lombok.RequiredArgsConstructor;
import org.openwes.simulator.websocket.RobotStateHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RobotStateHandler robotStateHandler;
    private final SimulatorProperties properties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] origins = properties.getCors().getAllowedOrigins().split(",");
        registry.addHandler(robotStateHandler, "/ws/robots")
                .setAllowedOrigins(origins);
    }
}
