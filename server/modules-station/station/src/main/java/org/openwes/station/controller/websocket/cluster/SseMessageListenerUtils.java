package org.openwes.station.controller.websocket.cluster;


import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.mq.MqClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseMessageListenerUtils {

    private final MqClient mqClient;

    public void noticeWorkStationVOChanged(Long workStationId) {
        mqClient.sendMessage(RedisConstants.STATION_LISTEN_STATION_WEBSOCKET, workStationId);
    }
}
