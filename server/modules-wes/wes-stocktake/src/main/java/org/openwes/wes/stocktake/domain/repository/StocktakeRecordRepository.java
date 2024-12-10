package org.openwes.wes.stocktake.domain.repository;

import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;

import java.util.Collection;
import java.util.List;

public interface StocktakeRecordRepository {

    List<StocktakeRecord> findAllByTaskDetailId(Long stocktakeTaskDetailId);

    StocktakeRecord save(StocktakeRecord stocktakeRecord);

    List<StocktakeRecord> saveAll(List<StocktakeRecord> newRecordList);

    StocktakeRecord findById(Long id);

    List<StocktakeRecord> findAllByTaskIdAndStatuses(Collection<Long> taskIdList, Collection<StocktakeRecordStatusEnum> statuses);
}
