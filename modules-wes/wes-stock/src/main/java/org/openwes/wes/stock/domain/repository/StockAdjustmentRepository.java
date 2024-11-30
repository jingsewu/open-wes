package org.openwes.wes.stock.domain.repository;

import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;

import java.util.List;

public interface StockAdjustmentRepository {

    StockAdjustmentOrder createOrderAndDetails(StockAdjustmentOrder stockAdjustmentOrder);

    List<StockAdjustmentOrder> findByIds(List<Long> ids);

    void saveOrders(List<StockAdjustmentOrder> stockAdjustmentOrders);

}
