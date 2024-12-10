package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAdjustmentOrderPORepository extends JpaRepository<StockAdjustmentOrderPO, Long> {
}
