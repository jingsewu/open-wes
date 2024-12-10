package org.openwes.station.controller.view;

import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.IViewHandler;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewHelper<T extends WorkStationCache> {

    private final List<IViewHandler<T>> iViewHandlers;
    private final RemoteWorkStationService remoteWorkStationService;
    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationCacheRepository;

    public WorkStationVO buildView(Long workStationId) {
        WorkStationDTO workStationDTO = remoteWorkStationService.queryWorkStation(workStationId);
        if (workStationDTO == null) {
            return null;
        }
        T workStationCache = workStationService.getWorkStation(workStationId);
        if (workStationCache == null) {
            log.warn("viewHelper buildView workStationCache is null");
            workStationCache = workStationService.initWorkStation(workStationDTO);
            workStationCacheRepository.save(workStationCache);
        }
        ViewContext<T> viewContext = new ViewContext<>(workStationDTO, workStationCache);
        List<IViewHandler<T>> viewHandlers = getViewHandlers(workStationDTO, workStationCache);
        for (IViewHandler<T> viewHandler : viewHandlers) {
            viewHandler.buildView(viewContext);
        }
        return viewContext.getWorkStationVO();
    }

    public List<IViewHandler<T>> getViewHandlers(WorkStationDTO workStationDTO, T workStationCache) {
        if (workStationDTO.getWorkStationStatus() == WorkStationStatusEnum.OFFLINE || Objects.isNull(workStationCache)) {
            return iViewHandlers.stream()
                    .filter(v -> ViewHandlerTypeEnum.baseViewHandlerTypes().contains(v.getViewTypeEnum())).toList();
        }
        if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.PICKING) {
            return iViewHandlers.stream()
                    .filter(v -> ViewHandlerTypeEnum.pickingViewHandlerTypes().contains(v.getViewTypeEnum())).toList();
        }
        if (WorkStationModeEnum.isPutAwayMode(workStationDTO.getWorkStationMode())) {
            return iViewHandlers.stream()
                    .filter(v -> ViewHandlerTypeEnum.replenishmentViewHandlerTypes().contains(v.getViewTypeEnum())).toList();
        }
        if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.RECEIVE) {
            return iViewHandlers.stream()
                    .filter(v -> ViewHandlerTypeEnum.baseViewHandlerTypes().contains(v.getViewTypeEnum())).toList();
        }
        if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.STOCKTAKE) {
            return iViewHandlers.stream()
                    .filter(v -> ViewHandlerTypeEnum.stocktakeViewHandlerTypes().contains(v.getViewTypeEnum())).toList();
        }
        return iViewHandlers;
    }

    public WorkStationVO getWorkStationVO(Long workStationId) {
        return buildView(workStationId);
    }
}
