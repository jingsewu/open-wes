package org.openwes.station.application.business.handler.common.extension.inbound;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.application.business.handler.common.OfflineHandler;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.infrastructure.remote.ContainerTaskService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InboundOfflineHandlerExtension implements OfflineHandler.Extension {

    private final EquipmentService equipmentService;
    private final ContainerTaskService containerTaskService;

    @Override
    public void doBeforeOffline(WorkStationCache workStationCache) {

        List<ArrivedContainerCache> arrivedContainers = workStationCache.getWorkLocationArea() != null
                ? workStationCache.getWorkLocationArea().getAllContainers()
                : Collections.emptyList();
        if (CollectionUtils.isNotEmpty(arrivedContainers)) {
            equipmentService.containerLeave(arrivedContainers, ContainerOperationTypeEnum.LEAVE);
        }

        if (ObjectUtils.isNotEmpty(workStationCache.getContainerTasks())) {
            containerTaskService.cancel(workStationCache.getContainerTasks().stream()
                    .filter(Objects::nonNull).map(WorkStationCache.ContainerTaskCache::getTaskCode).toList());
        }
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.SELECT_CONTAINER_PUT_AWAY;
    }
}
