package org.openwes.station.application.business.handler.common.extension.outbound;


import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class OutboundOperationTaskRefreshHandlerExtension
        implements OperationTaskRefreshHandler.Extension<OutboundWorkStationCache> {

    private final TaskService taskService;
    private final EquipmentService equipmentService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;

    @Override
    public void refresh(OutboundWorkStationCache workStationCache) {
        if (CollectionUtils.isNotEmpty(workStationCache.getOperateTasks())) {
            return;
        }

        workStationCache.clearOperateTasks();

        Collection<ArrivedContainerCache> doneContainers = workStationCache.queryTasksAndReturnRemovedContainers(taskService);

        if (CollectionUtils.isNotEmpty(doneContainers)) {
            equipmentService.containerLeave(doneContainers, ContainerOperationTypeEnum.LEAVE);
        }

        // clear choose area
        workStationCache.setChooseArea(null);

        workStationRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.PICKING;
    }

}
