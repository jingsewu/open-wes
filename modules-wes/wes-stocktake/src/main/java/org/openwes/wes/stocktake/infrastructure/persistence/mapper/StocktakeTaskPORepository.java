package org.openwes.wes.stocktake.infrastructure.persistence.mapper;

import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StocktakeTaskPORepository extends JpaRepository<StocktakeTaskPO, Long> {

    List<StocktakeTaskPO> findAllByWorkStationIdAndStocktakeTaskStatusIn(Long workStationId, Collection<StocktakeTaskStatusEnum> statuses);

    List<StocktakeTaskPO> findAllByStocktakeOrderId(Long stocktakeOrderId);
}
