package org.openwes.station.controller.websocket.controller;

import org.openwes.station.infrastructure.filters.HttpStationContext;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig serverEndpointConfig, HandshakeRequest request, HandshakeResponse response) {

        List<String> stationCodes = request.getHeaders().get(HttpStationContext.STATION_CODE);

        if (stationCodes == null || stationCodes.size() != 1) {
            stationCodes = request.getParameterMap().get(HttpStationContext.STATION_CODE);
        }

        if (stationCodes == null || stationCodes.size() != 1) {
            return;
        }

        String stationCode = stationCodes.get(0).trim();
        serverEndpointConfig.getUserProperties().put(HttpStationContext.STATION_CODE, stationCode);
        super.modifyHandshake(serverEndpointConfig, request, response);
    }
}
