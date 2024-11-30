package org.openwes.station.application.business.handler.common.extension.stocktake;

import com.google.common.collect.Lists;
import org.openwes.station.application.business.handler.common.OfflineHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaketakeOfflineHandlerExtension implements OfflineHandler.Extension<StocktakeWorkStationCache> {

    private final EquipmentService equipmentService;
    private final StocktakeService stocktakeService;

    @Override
    public void doBeforeOffline(StocktakeWorkStationCache workStationCache) {

        stocktakeService.closeStocktakeTasks(workStationCache.getId());

        List<ArrivedContainerCache> arrivedContainers = Optional.ofNullable(workStationCache.getArrivedContainers()).orElseGet(Lists::newArrayList);
        if (CollectionUtils.isNotEmpty(arrivedContainers)) {
            equipmentService.containerLeave(workStationCache.getArrivedContainers(), ContainerOperationTypeEnum.LEAVE);
        }
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.STOCKTAKE;
    }
}
