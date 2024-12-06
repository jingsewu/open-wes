package org.openwes.wes.stocktake.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.repository.StocktakeRecordRepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeRecordPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeRecordPO;
import org.openwes.wes.stocktake.infrastructure.persistence.transfer.StocktakeRecordPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakeRecordRepositoryImpl implements StocktakeRecordRepository {

    private final StocktakeRecordPORepository stocktakeRecordPORepository;
    private final StocktakeRecordPOTransfer stocktakeRecordPOTransfer;

    @Override
    public List<StocktakeRecord> findAllByTaskDetailId(Long stocktakeTaskDetailId) {
        List<StocktakeRecordPO> stocktakeRecordPOs = stocktakeRecordPORepository
                .findAllByStocktakeTaskDetailIdIn(Lists.newArrayList(stocktakeTaskDetailId));
        return stocktakeRecordPOTransfer.toDOS(stocktakeRecordPOs);
    }

    @Override
    public StocktakeRecord save(StocktakeRecord stocktakeRecord) {
        return stocktakeRecordPOTransfer.toDO(
                stocktakeRecordPORepository.save(stocktakeRecordPOTransfer.toPO(stocktakeRecord))
        );
    }

    @Override
    public List<StocktakeRecord> saveAll(List<StocktakeRecord> newRecordList) {
        return stocktakeRecordPOTransfer.toDOS(
                stocktakeRecordPORepository.saveAll(stocktakeRecordPOTransfer.toPOS(newRecordList))
        );
    }

    @Override
    public StocktakeRecord findById(Long id) {
        return stocktakeRecordPOTransfer.toDO(stocktakeRecordPORepository.findById(id).orElseThrow());
    }

    @Override
    public List<StocktakeRecord> findAllByTaskIdAndStatuses(Collection<Long> taskIdList, Collection<StocktakeRecordStatusEnum> statuses) {
        return stocktakeRecordPOTransfer.toDOS(stocktakeRecordPORepository.findAllByStocktakeTaskIdInAndStocktakeRecordStatusIn(taskIdList, statuses));
    }
}
