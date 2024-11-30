package org.openwes.wes.stocktake.infrastructure.persistence.mapper;

import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StocktakeOrderDetailPORepository extends JpaRepository<StocktakeOrderDetailPO, Long> {
    List<StocktakeOrderDetailPO> findAllByStocktakeOrderId(Long stocktakeOrderId);
}
