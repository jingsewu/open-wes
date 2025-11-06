package org.openwes.wes.common.facade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.mq.MqClient;
import org.openwes.wes.api.basic.event.PutWallAssignOrderEvent;
import org.openwes.wes.api.basic.event.PutWallRemindSealContainerEvent;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.springframework.stereotype.Service;

import static org.openwes.common.utils.constants.RedisConstants.STATION_LISTEN_CONTAINER_ARRIVED;

@Service
@RequiredArgsConstructor
public class StationCallbackFacade {

    private final MqClient mqClient;

    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        mqClient.sendMessage(STATION_LISTEN_CONTAINER_ARRIVED, containerArrivedEvent);
    }

    public void assignOrder(@Valid PutWallAssignOrderEvent event) {
        mqClient.sendMessage(RedisConstants.STATION_LISTEN_ORDER_ASSIGNED, event);
    }

    public void remindSealContainer(@Valid PutWallRemindSealContainerEvent event) {
        mqClient.sendMessage(RedisConstants.STATION_LISTEN_REMIND_TO_SEAL_CONTAINER, event);
    }
}
