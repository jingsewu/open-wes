package org.openwes.station.application.business.handler.stocktake;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StocktakeErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.application.business.handler.event.OperationTaskRefreshEvent;
import org.openwes.station.application.business.handler.event.stocktake.StocktakeSubmitEvent;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordSubmitDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeSubmitHandler implements IBusinessHandler<StocktakeSubmitEvent> {

    private final WorkStationService<StocktakeWorkStationCache> workStationService;
    private final WorkStationCacheRepository<StocktakeWorkStationCache> workStationCacheRepository;
    private final StocktakeService stocktakeService;
    private final OperationTaskRefreshHandler<StocktakeWorkStationCache> containerTaskRefreshHandler;

    @Override
    public void execute(StocktakeSubmitEvent body, Long workStationId) {
        StocktakeWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);

        Long detailId = body.getDetailId();

        OperationTaskVO operationTaskVO = workStationCache.getFirstOperationTaskVO();
        if (operationTaskVO == null) {
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_NO_OPERATION_TASK);
        }

        OperationTaskDTO operationTaskDTO = operationTaskVO.getOperationTaskDTO();
        if (!Objects.equals(detailId, operationTaskDTO.getId())) {
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_OPERATION_TASK_NOT_RIGHT);
        }

        StocktakeRecordSubmitDTO stocktakeRecordSubmitDTO = new StocktakeRecordSubmitDTO();
        stocktakeRecordSubmitDTO.setRecordId(detailId);
        stocktakeRecordSubmitDTO.setStocktakeQty(body.getStocktakeQty());
        stocktakeRecordSubmitDTO.setWorkStationId(workStationCache.getId());

        stocktakeService.submitStocktakeRecord(stocktakeRecordSubmitDTO);

        workStationCache.removeOperationTask(detailId);
        workStationCacheRepository.save(workStationCache);

        // task complete or task operated exception happened
        if ((ObjectUtils.isEmpty(workStationCache.getOperateTasks()) && ObjectUtils.isNotEmpty(workStationCache.getUndoContainers()))) {
            ArrivedContainerCache arrivedContainerCache = workStationCache.getUndoContainers().get(0);
            containerTaskRefreshHandler.execute(new OperationTaskRefreshEvent()
                    .setContainerCode(arrivedContainerCache.getContainerCode())
                    .setFace(arrivedContainerCache.getFace()), workStationCache.getId());
        }

    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.STOCKTAKE_SUBMIT;
    }

    @Override
    public Class<StocktakeSubmitEvent> getParameterClass() {
        return StocktakeSubmitEvent.class;
    }
}
