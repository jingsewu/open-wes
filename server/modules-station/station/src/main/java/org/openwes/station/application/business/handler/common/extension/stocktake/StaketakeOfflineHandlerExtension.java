package org.openwes.station.application.business.handler.common.extension.stocktake;

import org.openwes.station.application.business.handler.common.OfflineHandler;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaketakeOfflineHandlerExtension implements OfflineHandler.Extension {

    private final EquipmentService equipmentService;
    private final StocktakeService stocktakeService;

    @Override
    public void doBeforeOffline(WorkStationCache workStationCache) {

        stocktakeService.closeStocktakeTasks(workStationCache.getId());

        List<ArrivedContainerCache> arrivedContainers = workStationCache.getWorkLocationArea() != null
                ? workStationCache.getWorkLocationArea().getAllContainers()
                : Collections.emptyList();
        if (CollectionUtils.isNotEmpty(arrivedContainers)) {
            equipmentService.containerLeave(arrivedContainers, ContainerOperationTypeEnum.LEAVE);
        }
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.STOCKTAKE;
    }
}
