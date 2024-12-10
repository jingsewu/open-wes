package org.openwes.wes.stock.infrastructure.repository.impl;

import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.domain.repository.StockAbnormalRecordRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.StockAbnormalRecordPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAbnormalRecordPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.StockAbnormalRecordPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAbnormalRecordRepositoryImpl implements StockAbnormalRecordRepository {

    private final StockAbnormalRecordPORepository stockAbnormalRecordPORepository;
    private final StockAbnormalRecordPOTransfer stockAbnormalRecordPOTransfer;

    @Override
    public List<StockAbnormalRecord> saveAll(List<StockAbnormalRecord> stockAbnormalRecords) {
        List<StockAbnormalRecordPO> stockAbnormalRecordPOs = stockAbnormalRecordPORepository.saveAll(stockAbnormalRecordPOTransfer.toPOs(stockAbnormalRecords));
        return stockAbnormalRecordPOTransfer.toDOs(stockAbnormalRecordPOs);
    }

    @Override
    public List<StockAbnormalRecord> findByIds(Collection<Long> ids) {
        List<StockAbnormalRecordPO> stockAbnormalRecordPOs = stockAbnormalRecordPORepository.findAllById(ids);
        return stockAbnormalRecordPOTransfer.toDOs(stockAbnormalRecordPOs);
    }

    @Override
    public List<StockAbnormalRecord> findAllByContainerStockIdsAndStatues(Collection<Long> containerStockIds, List<StockAbnormalStatusEnum> stockAbnormalStatusEnums) {
        List<StockAbnormalRecordPO> stockAbnormalRecordPOs = stockAbnormalRecordPORepository.findAllByContainerStockIdInAndStockAbnormalStatusIn(containerStockIds, stockAbnormalStatusEnums);
        return stockAbnormalRecordPOTransfer.toDOs(stockAbnormalRecordPOs);
    }

}
