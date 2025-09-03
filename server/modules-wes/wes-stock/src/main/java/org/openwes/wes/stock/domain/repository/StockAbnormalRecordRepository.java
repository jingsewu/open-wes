package org.openwes.wes.stock.domain.repository;

import jakarta.validation.constraints.NotEmpty;
import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;

import java.util.Collection;
import java.util.List;

public interface StockAbnormalRecordRepository {
    List<StockAbnormalRecord> saveAll(List<StockAbnormalRecord> stockAbnormalRecords);

    List<StockAbnormalRecord> findByIds(Collection<Long> ids);

    List<StockAbnormalRecord> findAllByContainerStockIdsAndStatues(Collection<Long> containerStockIds, List<StockAbnormalStatusEnum> stockAbnormalStatusEnums);

    StockAbnormalRecord findByOrderNo(@NotEmpty String orderNo);

    List<StockAbnormalRecord> findAllByContainerStockId(Long stockId);
}
