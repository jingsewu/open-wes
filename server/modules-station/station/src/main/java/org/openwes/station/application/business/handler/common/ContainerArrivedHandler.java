package org.openwes.station.application.business.handler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.ContainerService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerArrivedHandler implements IBusinessHandler<ContainerArrivedEvent> {

    private final ContainerService containerService;
    private final WorkStationService workStationService;
    private final WorkStationCacheRepository workStationRepository;
    private final ExtensionFactory extensionFactory;
    private final EquipmentService equipmentService;

    @Override
    public void execute(ContainerArrivedEvent containerArrivedEvent, Long workStationId) {

        WorkStationCache workStation = workStationService.getWorkStation(workStationId);
        if (workStation == null) {
            log.warn("work station: {} is not exist or offline and let container: {} go", workStationId, containerArrivedEvent);
            equipmentService.containerLeave(containerArrivedEvent);
            return;
        }

        Set<Pair<String, String>> arrivedContainerCodes = new HashSet<>();
        List<ArrivedContainerCache> existingContainers = workStation.getWorkLocationArea().getAllContainers();
        if (existingContainers != null) {
            arrivedContainerCodes.addAll(existingContainers.stream()
                    .map(v -> Pair.of(v.getContainerCode(), v.getFace()))
                    .collect(Collectors.toSet()));
        }

        List<ArrivedContainerCache> arrivedContainers = containerArrivedEvent.getContainerDetails()
                .stream()
                .filter(v -> !arrivedContainerCodes.contains(Pair.of(v.getContainerCode(), v.getFace())))
                .map(containerDetail -> {
                    ContainerSpecDTO containerSpecDTO = containerService.queryContainerLayout(containerDetail.getContainerCode(), workStation.getWarehouseCode(), containerDetail.getFace());
                    ArrivedContainerCache arrivedContainerCache = toArrivedContainerCache(containerDetail, containerArrivedEvent, workStationId);
                    arrivedContainerCache.setContainerSpec(containerSpecDTO);
                    arrivedContainerCache.init();
                    return arrivedContainerCache;
                }).toList();

        // ignore repeat report
        if (CollectionUtils.isEmpty(arrivedContainers)) {
            log.warn("work station: {} code: {} container arrived repeated report.", workStationId, workStation.getStationCode());
            return;
        }

        boolean hasNonProceedContainers = existingContainers.stream()
                .anyMatch(v -> v.getProcessStatus() != org.openwes.station.api.constants.ProcessStatusEnum.PROCEED);

        if (!existingContainers.isEmpty() && hasNonProceedContainers) {
            arrivedContainers.forEach(c -> workStation.getWorkLocationArea().placeContainer(c));
            workStationRepository.save(workStation);
            return;
        }

        arrivedContainers.forEach(c -> workStation.getWorkLocationArea().placeContainer(c));
        OperationTaskRefreshHandler.Extension extension = extensionFactory.getExtension(workStation.getWorkStationMode(),
                ApiCodeEnum.CONTAINER_REFRESH);
        if (extension != null) {
            extension.refresh(workStation);
        }

        workStationRepository.save(workStation);
    }

    private ArrivedContainerCache toArrivedContainerCache(ContainerArrivedEvent.ContainerDetail detail,
                                                          ContainerArrivedEvent event, Long workStationId) {
        ArrivedContainerCache cache = new ArrivedContainerCache();
        cache.setWorkStationId(workStationId);
        cache.setContainerCode(detail.getContainerCode());
        cache.setFace(detail.getFace());
        cache.setLocationCode(detail.getLocationCode());
        cache.setWorkLocationCode(event.getWorkLocationCode());
        cache.setGroupCode(detail.getGroupCode() != null ? detail.getGroupCode() : "");
        cache.setRobotCode(detail.getRobotCode());
        cache.setRobotType(detail.getRobotType());
        cache.setLevel(detail.getLevel());
        cache.setBay(detail.getBay());
        cache.setForwardFace(detail.getForwardFace());
        return cache;
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CONTAINER_ARRIVED;
    }

    @Override
    public Class<ContainerArrivedEvent> getParameterClass() {
        return ContainerArrivedEvent.class;
    }


}
