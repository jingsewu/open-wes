package org.openwes.station.application.business.handler.common.extension.inbound;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.application.business.handler.common.OfflineHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.infrastructure.remote.ContainerTaskService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InboundOfflineHandlerExtension implements OfflineHandler.Extension<InboundWorkStationCache> {

    private final EquipmentService equipmentService;
    private final ContainerTaskService containerTaskService;

    @Override
    public void doBeforeOffline(InboundWorkStationCache workStationCache) {

        List<ArrivedContainerCache> arrivedContainers = Optional.ofNullable(workStationCache.getArrivedContainers()).orElseGet(Lists::newArrayList);
        if (CollectionUtils.isNotEmpty(arrivedContainers)) {
            equipmentService.containerLeave(workStationCache.getArrivedContainers(), ContainerOperationTypeEnum.LEAVE);
        }

        if(ObjectUtils.isNotEmpty(workStationCache.getTaskCodes())){
            containerTaskService.cancel(workStationCache.getTaskCodes());
        }
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.SELECT_CONTAINER_PUT_AWAY;
    }
}
