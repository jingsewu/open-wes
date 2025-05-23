package org.openwes.station.controller.websocket.controller;

import com.google.common.collect.Maps;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.infrastructure.filters.HttpStationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Slf4j
@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)
@Controller
public class StationWebSocketController {

    private Session session;

    private static final Map<String, StationWebSocketController> STATION_WEBSOCKET_MAP = Maps.newConcurrentMap();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {

        this.session = session;

        String stationCode;
        stationCode = (String) config.getUserProperties().get(HttpStationContext.STATION_CODE);

        if (stationCode == null) {
            Map<String, List<String>> parameterMap = session.getRequestParameterMap();
            List<String> stationCodes = parameterMap.get(HttpStationContext.STATION_CODE);
            if (ObjectUtils.isNotEmpty(stationCodes)) {
                stationCode = stationCodes.get(0);
            }
        }

        if (stationCode != null) {
            StationWebSocketController webSocketController = STATION_WEBSOCKET_MAP.get(stationCode);
            Optional.ofNullable(webSocketController).ifPresent(StationWebSocketController::closeSession);

            log.info("work station: {} websocket is open and websocket id is: {}.", stationCode, session.getId());

            //操作台连接后，将websocket对象加入到map中，如果相同操作台Code的连进来，直接覆盖
            STATION_WEBSOCKET_MAP.put(stationCode, this);

        } else {
            log.warn("client pass no stationCode, close session");

            session.close();
        }
    }

    private static void closeSession(StationWebSocketController stationWebSocketController) {
        Session session = stationWebSocketController.session;
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("close session error", e);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.info("websocket: {} close .", session == null ? "null" : session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("websocket: {} receive message: {}.", session.getId(), message);
        sendMessage("pong");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket err: ", error);
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                if (this.session.isOpen()) {
                    this.session.getBasicRemote().sendText(message == null ? "" : message);
                }
            } catch (IOException ioException) {
                log.error("station websocket send text error: ", ioException);
            }
        }
    }

    public boolean isOpen() {
        if (this.session == null) {
            log.debug("session is closed.");
            return false;
        }
        return this.session.isOpen();
    }

    public static StationWebSocketController getInstance(String stationCodeInfo) {
        return STATION_WEBSOCKET_MAP.get(stationCodeInfo);
    }

}

