package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StockAdjustmentDetailPORepository extends JpaRepository<StockAdjustmentDetailPO, Long> {

    List<StockAdjustmentDetailPO> findAllByStockAdjustmentOrderId(Long orderId);
    List<StockAdjustmentDetailPO> findAllByContainerStockIdIn(Collection<Long> containerStockIdList);
}
