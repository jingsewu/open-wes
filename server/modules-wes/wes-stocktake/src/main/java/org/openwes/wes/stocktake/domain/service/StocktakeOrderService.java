package org.openwes.wes.stocktake.domain.service;

import org.openwes.wes.api.stock.dto.StockAbnormalRecordDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordSubmitDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;

import java.util.List;

public interface StocktakeOrderService {

    List<StocktakeTask> splitStocktakeOrder(StocktakeOrder stocktakeOrder, Integer taskCount);

    void validateSubmit(StocktakeRecordSubmitDTO submitDTO, StocktakeRecord stocktakeRecord, List<StockAbnormalRecordDTO> stockAbnormalRecordDTOs);
}
