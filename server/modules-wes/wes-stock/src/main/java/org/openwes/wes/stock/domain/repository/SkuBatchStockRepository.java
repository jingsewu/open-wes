package org.openwes.wes.stock.domain.repository;

import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.springframework.data.domain.Page;
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

    List<SkuBatchStock> findAllBySkuIdAndWarehouseAreaIdAndGreaterThan(Long skuId, Long warehouseAreaId, int totalQty);

    List<SkuBatchStock> findAllBySkuIdsAndWarehouseAreaIdAndGreaterThan(Collection<Long> skuIds, Long warehouseAreaId, int totalQty);

    List<SkuBatchStock> findAllBySkuIdInAndWarehouseAreaIdInAndTotalQtyGreaterThan(Collection<Long> skuId, Collection<Long> warehouseAreaIds, int limitTotalQty);

    Page<SkuBatchStock> findAllByPage(Collection<Long> warehouseAreaIds, PageRequest pageRequest);

    void clearSkuBatchStockByIds(Set<Long> skuBatchStockIds);

    List<SkuBatchStock> findAllByWarehouseAreaIdInAndTotalQtyGreaterThan(Collection<Long> warehouseAreaIds, int limitTotalQty);
}
