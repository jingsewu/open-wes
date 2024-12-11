package org.openwes.wes.stock.domain.repository;

import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SkuBatchStockRepository {

    SkuBatchStock save(SkuBatchStock skuBatchStock);

    List<SkuBatchStock> saveAll(List<SkuBatchStock> toSkuBatchStocks);

    SkuBatchStock findById(Long skuBatchStockId);

    List<SkuBatchStock> findAllByIds(Collection<Long> skuBatchIds);

    List<SkuBatchStock> findAllBySkuBatchAttributeId(Long skuBatchAttributeId);

    List<SkuBatchStock> findAllBySkuBatchAttributeIds(Collection<Long> skuBatchAttributeIds);

    SkuBatchStock findBySkuBatchAttributeIdAndWarehouseAreaId(Long skuBatchAttributeId, Long warehouseAreaId);

    void clearSkuBatchStockByIds(Set<Long> skuBatchStockIds);

    void deleteAllZeroQtyStock(long expiredTime);
}
