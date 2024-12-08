package org.openwes.wes.common.facade;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.mq.MqClient;
import org.openwes.wes.api.basic.dto.PutWallSlotAssignedDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotRemindSealedDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.STATION_LISTEN_CONTAINER_ARRIVED;

@Service
@RequiredArgsConstructor
public class StationCallbackFacade {

    private final MqClient mqClient;

    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        mqClient.sendMessage(STATION_LISTEN_CONTAINER_ARRIVED, containerArrivedEvent);
    }

    public void assignOrder(List<PutWallSlotAssignedDTO> details) {
        mqClient.sendMessage(RedisConstants.STATION_LISTEN_ORDER_ASSIGNED, Lists.newArrayList(details));
    }

    public void remindSealContainer(List<PutWallSlotRemindSealedDTO> details) {
        mqClient.sendMessage(RedisConstants.STATION_LISTEN_REMIND_TO_SEAL_CONTAINER, Lists.newArrayList(details));
    }
}
