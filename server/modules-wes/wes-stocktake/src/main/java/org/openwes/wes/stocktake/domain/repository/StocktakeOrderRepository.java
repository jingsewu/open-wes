package org.openwes.wes.stocktake.domain.repository;

import org.openwes.wes.api.stocktake.constants.StocktakeOrderStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;

import java.util.Collection;
import java.util.List;

public interface StocktakeOrderRepository {

    StocktakeOrder saveStocktakeOrder(StocktakeOrder stocktakeOrder);

    List<StocktakeOrder> saveAllOrderAndDetails(List<StocktakeOrder> stocktakeOrderList);

    List<StocktakeOrder> findAllByOrderNosAndWarehouseCodeAndStatuses(Collection<String> orderNos, String warehouseCode, List<StocktakeOrderStatusEnum> statuses);

    StocktakeOrder findById(Long id);

}
