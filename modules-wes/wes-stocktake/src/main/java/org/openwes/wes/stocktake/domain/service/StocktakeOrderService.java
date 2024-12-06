package org.openwes.wes.stocktake.domain.service;

import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;

import java.util.List;

public interface StocktakeOrderService {

    List<StocktakeOrder> cancelStocktakeOrder(List<StocktakeOrder> stocktakeOrderList);

    List<StocktakeTask> splitStocktakeOrder(StocktakeOrder stocktakeOrder, Integer taskCount);

    void validateSubmit(StocktakeRecord stocktakeRecord);
}
