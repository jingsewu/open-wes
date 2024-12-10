package org.openwes.wes.stocktake.infrastructure.persistence.mapper;

import org.openwes.wes.api.stocktake.constants.StocktakeOrderStatusEnum;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StocktakeOrderPORepository extends JpaRepository<StocktakeOrderPO, Long> {
    List<StocktakeOrderPO> findAllByWarehouseCodeAndOrderNoInAndStocktakeOrderStatusIn(String warehouseCode, Collection<String> orderNo, Collection<StocktakeOrderStatusEnum> statuses);
}
