package org.openwes.wes.stocktake.domain.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.stock.dto.StockAbnormalRecordDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeUnitTypeEnum;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordSubmitDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.service.StocktakeOrderService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.StocktakeErrorDescEnum.STOCKTAKE_EXCEEDING_THE_MIN_STOCKTAKE_LOSS_QTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocktakeOrderServiceImpl implements StocktakeOrderService {

    private final IStockApi stockApi;
    private final IContainerApi containerApi;

    @Override
    public List<StocktakeTask> splitStocktakeOrder(StocktakeOrder stocktakeOrder, Integer taskCount) {

        List<String> containerCodes;
        List<ContainerStockDTO> containerStockList;

        if (stocktakeOrder.getStocktakeUnitType() == StocktakeUnitTypeEnum.SKU) {
            containerStockList = stockApi.getBySkuIds(stocktakeOrder.getAllStocktakeUnitIds());
            containerCodes = containerStockList.stream().map(ContainerStockDTO::getContainerCode).distinct().toList();

            Collection<ContainerDTO> containers = containerApi.queryContainer(containerCodes, stocktakeOrder.getWarehouseCode());
            containerCodes = containers.stream().filter(v -> Objects.equals(v.getWarehouseAreaId(), stocktakeOrder.getWarehouseAreaId()))
                    .map(ContainerDTO::getContainerCode).toList();

            List<String> finalContainerCodes = containerCodes;
            containerStockList = containerStockList.stream().filter(v -> finalContainerCodes.contains(v.getContainerCode())).toList();

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
                        StocktakeTask.createFromOrder(stocktakeOrder, containerFaceMap, subContainerCodes))
                .toList();
        for (int i = 0; i < stocktakeTasks.size(); i++) {
            stocktakeTasks.get(i).initialize(stocktakeOrder.getOrderNo(), i);
        }

        return stocktakeTasks;
    }

    @Override
    public void validateSubmit(StocktakeRecordSubmitDTO submitDTO, StocktakeRecord stocktakeRecord,
                               List<StockAbnormalRecordDTO> stockAbnormalRecordDTOs) {
        if (Objects.equals(stocktakeRecord.getQtyOriginal(), submitDTO.getStocktakeQty())) {
            return;
        }

        List<ContainerStockDTO> containerStocks = stockApi.getContainerStocks(Lists.newArrayList(stocktakeRecord.getStockId()));
        if (containerStocks.isEmpty()) {
            throw new IllegalStateException("container stock not found");
        }
        ContainerStockDTO containerStockDTO = containerStocks.get(0);
        SkuBatchStockDTO skuBatchStock = stockApi.getSkuBatchStock(containerStockDTO.getSkuBatchStockId());

        int availableQty = Math.min(skuBatchStock.getAvailableQty(), containerStockDTO.getAvailableQty());

        Integer totalQtyAbnormal = stockAbnormalRecordDTOs.stream().map(StockAbnormalRecordDTO::getQtyAbnormal)
                .reduce(Integer::sum).orElse(0);

        if (availableQty + totalQtyAbnormal < stocktakeRecord.getQtyOriginal() - submitDTO.getStocktakeQty()) {
            throw WmsException.throwWmsException(STOCKTAKE_EXCEEDING_THE_MIN_STOCKTAKE_LOSS_QTY, stocktakeRecord.getQtyOriginal() - availableQty - totalQtyAbnormal);
        }

    }
}
