package org.openwes.wes.stocktake.domain.service.impl;

import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeUnitTypeEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;
import org.openwes.wes.stocktake.domain.service.StocktakeRecordService;
import org.openwes.wes.stocktake.infrastructure.persistence.transfer.StocktakeRecordPOTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocktakeRecordServiceImpl implements StocktakeRecordService {

    private final StocktakeRecordPOTransfer stocktakeRecordPOTransfer;

    @Override
    public List<StocktakeRecord> generateStocktakeRecords(StocktakeTaskDetail stocktakeTaskDetail, StocktakeOrder stocktakeOrder, List<ContainerStockDTO> containerStockDTOs) {

        List<Long> allStocktakeUnitIds = stocktakeOrder.getAllStocktakeUnitIds();
        if (stocktakeOrder.getStocktakeUnitType() == StocktakeUnitTypeEnum.STOCK) {
            containerStockDTOs = containerStockDTOs.stream().filter(v -> allStocktakeUnitIds.contains(v.getId())).toList();
        } else if (stocktakeOrder.getStocktakeUnitType() == StocktakeUnitTypeEnum.SKU) {
            containerStockDTOs = containerStockDTOs.stream().filter(v -> allStocktakeUnitIds.contains(v.getSkuId())).toList();
        }

        List<StocktakeRecord> stocktakeRecords = containerStockDTOs.stream().map(stocktakeRecordPOTransfer::toDO).toList();

        stocktakeRecords.forEach(v -> v.initialize(stocktakeTaskDetail));

        return stocktakeRecords;
    }

}
