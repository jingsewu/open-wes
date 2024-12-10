package org.openwes.wes.stocktake.infrastructure.persistence.mapper;

import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeTaskDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StocktakeTaskDetailPORepository extends JpaRepository<StocktakeTaskDetailPO, Long> {

    List<StocktakeTaskDetailPO> findAllByStocktakeTaskIdIn(Collection<Long> stocktakeTaskIdList);

    List<StocktakeTaskDetailPO> findAllByContainerCodeAndContainerFaceAndStocktakeTaskIdIn(String containerCode, String face, List<Long> stocktakeTaskIds);
}
