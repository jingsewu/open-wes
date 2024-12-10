package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.SkuBatchStockPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkuBatchStockPORepository extends JpaRepository<SkuBatchStockPO, Long> {

    List<SkuBatchStockPO> findAllBySkuBatchAttributeId(Long skuBatchAttributeId);

    SkuBatchStockPO findBySkuBatchAttributeIdAndWarehouseAreaId(Long skuBatchAttributeId, Long warehouseAreaId);

    List<SkuBatchStockPO> findAllBySkuBatchAttributeIdIn(Collection<Long> skuBatchAttributeIds);

    List<SkuBatchStockPO> findAllBySkuIdAndWarehouseAreaIdAndTotalQtyGreaterThan(Long skuId, Long warehouseAreaId, Integer totalQty);

    List<SkuBatchStockPO> findAllBySkuIdInAndWarehouseAreaIdAndTotalQtyGreaterThan(Collection<Long> skuId, Long warehouseAreaId, Integer totalQty);

    List<SkuBatchStockPO> findAllBySkuIdInAndWarehouseAreaIdInAndTotalQtyGreaterThan(Collection<Long> skuId, Collection<Long> warehouseAreaId, Integer totalQty);

    Page<SkuBatchStockPO> findAllByWarehouseAreaIdIn(Collection<Long> warehouseAreaId, Pageable pageable);

    List<SkuBatchStockPO> findAllByWarehouseAreaIdInAndTotalQtyGreaterThan(Collection<Long> warehouseAreaIds, int limitTotalQty);
}
