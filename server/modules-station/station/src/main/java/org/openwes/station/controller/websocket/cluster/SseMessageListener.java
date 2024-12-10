package org.openwes.station.controller.websocket.cluster;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.mq.redis.RedisListener;
import org.openwes.station.controller.websocket.controller.StationWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseMessageListener {

    /**
     * TODO by Kinser
     *  we can group the same workStationId and just refresh once.
     *  maybe one of the available approaches is fetching more then one message once, then group the same workStationId
     */
    @RedisListener(topic = RedisConstants.STATION_LISTEN_STATION_WEBSOCKET, type = Long.class)
    public void onMessage(String topic, Long workStationId) {

        if (workStationId == null) {
            log.error("received work station id is null");
            return;
        }
        StationWebSocketController stationWebSocketController = StationWebSocketController.getInstance(String.valueOf(workStationId));

        if (stationWebSocketController != null) {
            log.info("station: {} send message to websocket: {}.", workStationId,
                    stationWebSocketController.getSession() == null ? "NULL" : stationWebSocketController.getSession().getId());

            stationWebSocketController.sendMessage("changed");
        } else {
            log.debug("StationWebSocketUtils STATION_WEBSOCKET does not exist! station: {}, message:{}", workStationId, "changed");
        }
    }
}
