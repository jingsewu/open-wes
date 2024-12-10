package org.openwes.wes.stocktake.domain.service;

import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;

import java.util.List;

public interface StocktakeRecordService {

    List<StocktakeRecord> generateStocktakeRecords(StocktakeTaskDetail stocktakeTaskDetail, StocktakeOrder stocktakeOrder,
                                                   List<ContainerStockDTO> containerStockDTOs);
}
