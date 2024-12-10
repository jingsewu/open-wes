package org.openwes.station.application.business.handler.common.extension.stocktake;

import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakeOperationTaskRefreshHandlerExtension implements OperationTaskRefreshHandler.Extension<StocktakeWorkStationCache> {

    private final StocktakeService stocktakeService;
    private final EquipmentService equipmentService;
    private final WorkStationCacheRepository<StocktakeWorkStationCache> workStationRepository;

    @Override
    public void refresh(StocktakeWorkStationCache workStationCache) {
        List<ArrivedContainerCache> arrivedContainers = workStationCache.getArrivedContainers();
        if (ObjectUtils.isEmpty(arrivedContainers)) {
            return;
        }

        Collection<ArrivedContainerCache> doneContainers = workStationCache.queryTasksAndReturnRemovedContainers(stocktakeService);

        if (CollectionUtils.isNotEmpty(doneContainers)) {
            equipmentService.containerLeave(doneContainers, ContainerOperationTypeEnum.LEAVE);
        }

        workStationRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.STOCKTAKE;
    }
}
