package org.openwes.wes.stocktake.domain.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskDetailStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeUnitTypeEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;
import org.openwes.wes.stocktake.domain.service.StocktakeOrderService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.StocktakeErrorDescEnum.STOCKTAKE_EXCEEDING_THE_MIN_STOCKTAKE_LOSS_QTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocktakeOrderServiceImpl implements StocktakeOrderService {

    private final IStockApi stockApi;

    @Override
    public List<StocktakeOrder> cancelStocktakeOrder(List<StocktakeOrder> stocktakeOrderList) {
        stocktakeOrderList.forEach(StocktakeOrder::cancel);
        return stocktakeOrderList;
    }

    @Override
    public List<StocktakeTask> splitStocktakeOrder(StocktakeOrder stocktakeOrder, Integer taskCount) {

        List<String> containerCodes;
        List<ContainerStockDTO> containerStockList;

        if (stocktakeOrder.getStocktakeUnitType() == StocktakeUnitTypeEnum.SKU) {
            containerStockList = stockApi.getBySkuIds(stocktakeOrder.getAllStocktakeUnitIds());
            containerCodes = containerStockList.stream().map(ContainerStockDTO::getContainerCode).distinct().toList();

        } else if (stocktakeOrder.getStocktakeUnitType() == StocktakeUnitTypeEnum.STOCK) {
            containerStockList = stockApi.getContainerStocks(stocktakeOrder.getAllStocktakeUnitIds());
            containerCodes = containerStockList.stream().map(ContainerStockDTO::getContainerCode).distinct().toList();
        } else {
            containerCodes = stocktakeOrder.getAllStocktakeUnitCodes();
            containerStockList = stockApi.getContainerStocks(containerCodes, stocktakeOrder.getWarehouseCode());
        }

        if (ObjectUtils.isEmpty(containerCodes) || ObjectUtils.isEmpty(containerStockList)) {
            log.warn("stock order: {} can not found any stocks, so can not create any stock tasks.", stocktakeOrder.getOrderNo());
            return Collections.emptyList();
        }

        Map<String, Set<String>> containerFaceMap = containerStockList.stream().collect(Collectors.groupingBy(
                ContainerStockDTO::getContainerCode,
                Collectors.mapping(ContainerStockDTO::getContainerFace, Collectors.toSet())
        ));

        List<StocktakeTask> stocktakeTasks = Lists.partition(containerCodes, taskCount).stream().map(subContainerCodes ->
                        buildStocktakeTask(stocktakeOrder, containerFaceMap, subContainerCodes))
                .toList();
        for (int i = 0; i < stocktakeTasks.size(); i++) {
            stocktakeTasks.get(i).initialize(stocktakeOrder.getOrderNo(), i);
        }

        return stocktakeTasks;
    }

    @Override
    public void validateSubmit(StocktakeRecord stocktakeRecord) {
        if (stocktakeRecord.getQtyAbnormal() <= 0) {
            return;
        }

        List<ContainerStockDTO> containerStocks = stockApi.getContainerStocks(Lists.newArrayList(stocktakeRecord.getStockId()));
        if (containerStocks.isEmpty()) {
            throw new IllegalStateException("container stock not found");
        }

        ContainerStockDTO containerStockDTO = containerStocks.get(0);
        if (containerStockDTO.getAvailableQty() < stocktakeRecord.getQtyAbnormal()) {
            throw WmsException.throwWmsException(STOCKTAKE_EXCEEDING_THE_MIN_STOCKTAKE_LOSS_QTY,
                    containerStockDTO.getOutboundLockedQty(), containerStockDTO.getNoOutboundLockedQty(), containerStockDTO.getAvailableQty());
        }

    }

    private StocktakeTask buildStocktakeTask(StocktakeOrder stocktakeOrder, Map<String, Set<String>> containerFaceMap,
                                             List<String> subContainerCodes) {
        List<StocktakeTaskDetail> stocktakeTaskDetails = subContainerCodes.stream().flatMap(containerCode ->
                containerFaceMap.get(containerCode).stream().map(containerFace -> {
                    StocktakeTaskDetail stocktakeTaskDetail = new StocktakeTaskDetail();
                    stocktakeTaskDetail.setContainerCode(containerCode);
                    stocktakeTaskDetail.setStocktakeOrderId(stocktakeOrder.getId());
                    stocktakeTaskDetail.setContainerFace(containerFace);
                    stocktakeTaskDetail.setStocktakeTaskDetailStatus(StocktakeTaskDetailStatusEnum.NEW);
                    stocktakeTaskDetail.setWarehouseCode(stocktakeOrder.getWarehouseCode());
                    return stocktakeTaskDetail;
                }).toList().stream()).toList();
        StocktakeTask stocktakeTask = new StocktakeTask();
        stocktakeTask.setStocktakeMethod(stocktakeOrder.getStocktakeMethod());
        stocktakeTask.setStocktakeUnitType(stocktakeOrder.getStocktakeUnitType());
        stocktakeTask.setStocktakeType(stocktakeOrder.getStocktakeType());
        stocktakeTask.setStocktakeCreateMethod(stocktakeOrder.getStocktakeCreateMethod());
        stocktakeTask.setStocktakeOrderId(stocktakeOrder.getId());
        stocktakeTask.setStocktakeTaskStatus(StocktakeTaskStatusEnum.NEW);
        stocktakeTask.setWarehouseCode(stocktakeOrder.getWarehouseCode());
        stocktakeTask.setDetails(stocktakeTaskDetails);
        return stocktakeTask;
    }

}
