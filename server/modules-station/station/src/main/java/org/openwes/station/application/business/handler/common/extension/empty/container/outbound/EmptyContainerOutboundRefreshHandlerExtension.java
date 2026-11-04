package org.openwes.station.application.business.handler.common.extension.empty.container.outbound;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.ContainerService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmptyContainerOutboundRefreshHandlerExtension implements OperationTaskRefreshHandler.Extension {

    private final EquipmentService equipmentService;
    private final ContainerService containerService;
    private final WorkStationCacheRepository workStationCacheRepository;

    @Override
    public void refresh(WorkStationCache workStationCache) {
        List<ArrivedContainerCache> arrivedContainers = workStationCache.getWorkLocationArea().getAllContainers();
        if (ObjectUtils.isEmpty(arrivedContainers)) {
            return;
        }

        Set<String> containerCodes = arrivedContainers.stream().map(ArrivedContainerCache::getContainerCode).collect(Collectors.toSet());

        containerService.moveOutside(workStationCache.getWarehouseCode(), containerCodes);
        equipmentService.containerLeave(arrivedContainers, ContainerOperationTypeEnum.MOVE_OUT);

        containerCodes.forEach(code -> workStationCache.getWorkLocationArea().removeContainer(code));
        workStationCacheRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.EMPTY_CONTAINER_OUTBOUND;
    }
}
