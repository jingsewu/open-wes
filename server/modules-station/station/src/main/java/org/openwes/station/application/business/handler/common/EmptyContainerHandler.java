package org.openwes.station.application.business.handler.common;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.EmptyContainerHandleEvent;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.ContainerService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.openwes.station.api.constants.ApiCodeEnum.EMPTY_CONTAINER_HANDLE;

@Service
@RequiredArgsConstructor
public class EmptyContainerHandler<T extends WorkStationCache> implements IBusinessHandler<EmptyContainerHandleEvent> {

    private final ContainerService containerService;
    private final WorkStationService<T> workStationService;
    private final EquipmentService equipmentService;
    private final WorkStationCacheRepository<T> workStationCacheRepository;

    @Override
    public void execute(EmptyContainerHandleEvent event, Long workStationId) {

        T workStationCache = workStationService.getOrThrow(workStationId);

        if (event.getContainerOperationType() == ContainerOperationTypeEnum.MOVE_OUT) {
            containerService.moveOutside(workStationCache.getWarehouseCode(), Sets.newHashSet(event.getContainerCode()));
        }

        List<ArrivedContainerCache> arrivedContainers = workStationCache.getArrivedContainers().stream().filter(v -> v.getContainerCode().equals(event.getContainerCode())).toList();
        equipmentService.containerLeave(arrivedContainers, ContainerOperationTypeEnum.MOVE_OUT);

        workStationCache.clearArrivedContainers(Sets.newHashSet(event.getContainerCode()));
        workStationCacheRepository.save(workStationCache);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return EMPTY_CONTAINER_HANDLE;
    }

    @Override
    public Class<EmptyContainerHandleEvent> getParameterClass() {
        return EmptyContainerHandleEvent.class;
    }
}
