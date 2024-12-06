package org.openwes.station.controller.view.handler.impl.stocktake;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.BaseAreaHandler;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.basic.constants.WorkStationProcessingStatusEnum;
import org.openwes.wes.api.stocktake.dto.StocktakeTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeBaseAreaHandler extends BaseAreaHandler<StocktakeWorkStationCache> {

    private final StocktakeService stocktakeService;

    @Override
    protected void setChooseArea(ViewContext<StocktakeWorkStationCache> viewContext) {
        StocktakeWorkStationCache workStationCache = viewContext.getWorkStationCache();
        WorkStationVO workStationVO = viewContext.getWorkStationVO();
        workStationVO.setScanCode(workStationCache.getScannedBarcode());

        WorkStationVO.ChooseAreaEnum chooseArea = workStationCache.getChooseArea();
        if (chooseArea == null) {
            if (CollectionUtils.isNotEmpty(workStationCache.getOperateTasks())) {
                chooseArea = WorkStationVO.ChooseAreaEnum.SKU_AREA;
            }
        }

        if (chooseArea == null) {
            chooseArea = WorkStationVO.ChooseAreaEnum.CONTAINER_AREA;
        }
        workStationVO.setChooseArea(chooseArea);
    }

    protected void setOrderArea(ViewContext<StocktakeWorkStationCache> viewContext) {
        StocktakeWorkStationCache workStationCache = viewContext.getWorkStationCache();
        List<StocktakeTaskDTO> stocktakeTasks = stocktakeService.getStocktakeTasks(workStationCache.getId());
        if (CollectionUtils.isEmpty(stocktakeTasks)) {
            return;
        }

        List<OperationTaskVO> operateTasks = workStationCache.getOperateTasks();

        StocktakeTaskDTO currentTask = stocktakeTasks.get(0);
        if (CollectionUtils.isNotEmpty(operateTasks)) {
            OperationTaskVO operationTaskVO = workStationCache.getFirstProcessingTask();
            if (operationTaskVO == null) {
                operationTaskVO = workStationCache.getOperateTasks().get(0);
            }
            OperationTaskVO finalOperationTaskVO = operationTaskVO;
            currentTask = stocktakeTasks.stream().filter(stocktakeTaskDTO ->
                            stocktakeTaskDTO.getId().equals(finalOperationTaskVO.getOperationTaskDTO().getOrderId()))
                    .findFirst().orElse(null);
        }

        if (currentTask != null) {
            WorkStationVO.OrderArea orderArea = WorkStationVO.OrderArea.builder().currentStocktakeOrder(WorkStationVO.StocktakeOrderVO.builder()
                    .stocktakeMethod(currentTask.getStocktakeMethod())
                    .stocktakeType(currentTask.getStocktakeType())
                    .stocktakeCreateMethod(currentTask.getStocktakeCreateMethod())
                    .taskNo(currentTask.getTaskNo())
                    .build()).build();

            viewContext.getWorkStationVO().setOperationOrderArea(orderArea);
        }

    }

    @Override
    public void setStationProcessingStatus(WorkStationVO workStationVO, StocktakeWorkStationCache workStationCache) {

        if (workStationVO.getOperationOrderArea() == null || workStationVO.getOperationOrderArea().getCurrentStocktakeOrder() == null) {
            workStationVO.setStationProcessingStatus(WorkStationProcessingStatusEnum.NO_TASK);
            return;
        }

        if (ObjectUtils.isNotEmpty(workStationCache.getArrivedContainers())) {
            return;
        }

        workStationVO.setStationProcessingStatus(WorkStationProcessingStatusEnum.WAIT_ROBOT);
    }

    @Override
    protected void setToolbar(ViewContext<StocktakeWorkStationCache> viewContext) {
        super.setToolbar(viewContext);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.STOCKTAKE_BASE_AREA;
    }
}
