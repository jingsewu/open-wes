package org.openwes.wes.stocktake.infrastructure.persistence.mapper;

import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeRecordPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StocktakeRecordPORepository extends JpaRepository<StocktakeRecordPO, Long> {
    List<StocktakeRecordPO> findAllByStocktakeTaskDetailIdIn(Collection<Long> taskDetailIdList);

    List<StocktakeRecordPO> findAllByStocktakeTaskIdInAndStocktakeRecordStatusIn(Collection<Long> taskIdList,
                                                                                 Collection<StocktakeRecordStatusEnum> statuses);
}
