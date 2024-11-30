package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.domain.transfer.ArriveContainerCacheTransfer;
import org.openwes.station.infrastructure.remote.ContainerService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerArrivedHandler<T extends WorkStationCache> implements IBusinessHandler<ContainerArrivedEvent> {

    private final ContainerService containerService;
    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationRepository;
    private final ArriveContainerCacheTransfer arriveContainerCacheTransfer;
    private final ExtensionFactory extensionFactory;
    private final EquipmentService equipmentService;

    @Override
    public void execute(ContainerArrivedEvent containerArrivedEvent, Long workStationId) {

        T workStation = workStationService.getWorkStation(workStationId);
        if (workStation == null) {
            log.info("work station: {} is not exist or offline and let container: {} go", workStationId, containerArrivedEvent);
            equipmentService.containerLeave(containerArrivedEvent);
            return;
        }

        Set<Pair<String, String>> arrivedContainerCodes = new HashSet<>();
        if (workStation.getArrivedContainers() != null) {
            arrivedContainerCodes.addAll(workStation.getArrivedContainers().stream()
                    .map(v -> Pair.of(v.getContainerCode(), v.getFace()))
                    .collect(Collectors.toSet()));
        }

        List<ArrivedContainerCache> arrivedContainers = containerArrivedEvent.getContainerDetails()
                .stream()
                .filter(v -> !arrivedContainerCodes.contains(Pair.of(v.getContainerCode(), v.getFace())))
                .map(containerDetail -> {
                    ContainerSpecDTO containerSpecDTO = containerService.queryContainerLayout(containerDetail.getContainerCode(), workStation.getWarehouseCode(), containerDetail.getFace());
                    ArrivedContainerCache arrivedContainerCache = arriveContainerCacheTransfer.toDTO(containerDetail, containerArrivedEvent);
                    arrivedContainerCache.setContainerSpec(containerSpecDTO);
                    arrivedContainerCache.init();
                    return arrivedContainerCache;
                }).toList();

        // ignore repeat report
        if (CollectionUtils.isEmpty(arrivedContainers)) {
            return;
        }

        workStation.addArrivedContainers(arrivedContainers);

        OperationTaskRefreshHandler.Extension<T> extension = extensionFactory.getExtension(workStation.getWorkStationMode(),
                ApiCodeEnum.CONTAINER_REFRESH);
        if (extension != null) {
            extension.refresh(workStation);
        }

        workStationRepository.save(workStation);
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
