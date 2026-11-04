package org.openwes.station.application.business.handler.common.extension.stocktake;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.ContainerService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StocktakeOperationTaskRefreshHandlerExtension implements OperationTaskRefreshHandler.Extension {

    private final StocktakeService stocktakeService;
    private final EquipmentService equipmentService;
    private final ContainerService containerService;

    @Override
    public void refresh(WorkStationCache workStationCache) {
        if (!(workStationCache instanceof StocktakeWorkStationCache stocktakeCache)) {
            return;
        }

        List<ArrivedContainerCache> arrivedContainers = stocktakeCache.getWorkLocationArea().getAllContainers();
        if (ObjectUtils.isEmpty(arrivedContainers)) {
            return;
        }

        Collection<ArrivedContainerCache> doneContainers = stocktakeCache.queryTasksAndReturnRemovedContainers(stocktakeService);
        if (CollectionUtils.isNotEmpty(doneContainers)) {
            containerService.unLockContainer(stocktakeCache.getWarehouseCode(),
                    doneContainers.stream().map(ArrivedContainerCache::getContainerCode).collect(Collectors.toSet()));
            equipmentService.containerLeave(doneContainers, ContainerOperationTypeEnum.LEAVE);
        }

    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.STOCKTAKE;
    }
}
