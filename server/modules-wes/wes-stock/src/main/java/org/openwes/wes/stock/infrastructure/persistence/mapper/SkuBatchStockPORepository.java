package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.SkuBatchStockPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkuBatchStockPORepository extends JpaRepository<SkuBatchStockPO, Long> {

    List<SkuBatchStockPO> findAllBySkuBatchAttributeId(Long skuBatchAttributeId);

    SkuBatchStockPO findBySkuBatchAttributeIdAndWarehouseAreaId(Long skuBatchAttributeId, Long warehouseAreaId);

    List<SkuBatchStockPO> findAllBySkuBatchAttributeIdIn(Collection<Long> skuBatchAttributeIds);

    void deleteAllByUpdateTimeBeforeAndTotalQty(long expiredTime, int totalQty);
}
