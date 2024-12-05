package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAbnormalRecordPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StockAbnormalRecordPORepository extends JpaRepository<StockAbnormalRecordPO, Long> {

    List<StockAbnormalRecordPO> findAllByContainerStockIdInAndStockAbnormalStatusIn(Collection<Long> containerStockIds, Collection<StockAbnormalStatusEnum> statuses);
}
