package org.openwes.station.infrastructure.remote;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.mq.redis.RedisListener;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.PtlApiImpl;
import org.openwes.station.application.executor.HandlerExecutor;
import org.openwes.station.controller.websocket.cluster.SseMessageListenerUtils;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.wes.api.basic.dto.PutWallSlotAssignedDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotRemindSealedDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkStationMqConsumer<T extends WorkStationCache> {

    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationCacheRepository;
    private final PtlApiImpl ptlService;
    private final SseMessageListenerUtils sseMessageListenerUtils;
    private final HandlerExecutor handlerExecutor;

    @RedisListener(topic = RedisConstants.STATION_LISTEN_CONTAINER_ARRIVED, type = ContainerArrivedEvent.class)
    public void listenContainerArrived(String topic, ContainerArrivedEvent containerArrivedEvent) {
        if (containerArrivedEvent == null) {
            return;
        }

        T workStation = workStationService.getWorkStation(containerArrivedEvent.getWorkStationId());
        if (workStation == null) {
            return;
        }

        handlerExecutor.execute(ApiCodeEnum.CONTAINER_ARRIVED, JsonUtils.obj2String(containerArrivedEvent),
                containerArrivedEvent.getWorkStationId());
    }

    @RedisListener(topic = RedisConstants.STATION_LISTEN_WORK_STATION_CONFIG_UPDATE, type = WorkStationConfigDTO.class)
    public void listenWorkStationConfigUpdated(String topic, WorkStationConfigDTO workStationConfigDTO) {
        if (workStationConfigDTO == null) {
            return;
        }

        T workStation = workStationService.getWorkStation(workStationConfigDTO.getWorkStationId());
        if (workStation == null) {
            return;
        }

        workStation.updateConfiguration(workStationConfigDTO);
        workStationCacheRepository.save(workStation);
    }

    @RedisListener(topic = RedisConstants.STATION_LISTEN_ORDER_ASSIGNED, type = List.class)
    public void listenOrderAssigned(String topic, List<PutWallSlotAssignedDTO> putWallSlotDTOS) {

        if (CollectionUtils.isEmpty(putWallSlotDTOS)) {
            return;
        }

        Map<Long, List<PutWallSlotAssignedDTO>> stationCodeMap = putWallSlotDTOS.stream().collect(Collectors.groupingBy(PutWallSlotAssignedDTO::getWorkStationId));

        stationCodeMap.forEach((workStationId, values) -> {
            WorkStationCache workStation = workStationService.getWorkStation(workStationId);
            if (workStation == null) {
                return;
            }

            values.forEach(v -> ptlService.reminderBind(workStationId, v.getPtlTag()));

            sseMessageListenerUtils.noticeWorkStationVOChanged(workStationId);
        });
    }

    @RedisListener(topic = RedisConstants.STATION_LISTEN_REMIND_TO_SEAL_CONTAINER)
    public void listenRemindToSealContainer(String topic, List<PutWallSlotRemindSealedDTO> putWallSlots) {

        if (CollectionUtils.isEmpty(putWallSlots)) {
            return;
        }

        putWallSlots.forEach(putWallSlot -> {
            WorkStationCache workStation = workStationService.getWorkStation(putWallSlot.getWorkStationId());
            if (workStation == null) {
                return;
            }

            ptlService.reminderSeal(workStation.getId(), putWallSlot.getPtlTag());

            sseMessageListenerUtils.noticeWorkStationVOChanged(workStation.getId());
        });
    }
}
