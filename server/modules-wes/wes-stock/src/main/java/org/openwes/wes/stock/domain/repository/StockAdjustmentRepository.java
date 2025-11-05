package org.openwes.wes.stock.domain.repository;

import jakarta.validation.constraints.NotEmpty;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;

import java.util.List;

public interface StockAdjustmentRepository {

    StockAdjustmentOrder saveOrderAndDetails(StockAdjustmentOrder stockAdjustmentOrder);

    void saveOrders(List<StockAdjustmentOrder> stockAdjustmentOrders);

    StockAdjustmentOrder findById(Long id);

    List<StockAdjustmentOrder> findByIds(List<Long> ids);

    StockAdjustmentOrder findByOrderNo(@NotEmpty String orderNo);
}
