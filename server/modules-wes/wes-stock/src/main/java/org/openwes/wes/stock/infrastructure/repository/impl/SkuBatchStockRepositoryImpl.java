package org.openwes.wes.stock.infrastructure.repository.impl;

import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.SkuBatchStockPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.SkuBatchStockPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.SkuBatchStockPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SkuBatchStockRepositoryImpl implements SkuBatchStockRepository {

    private final SkuBatchStockPORepository skuBatchStockPORepository;
    private final SkuBatchStockPOTransfer skuBatchStockPOTransfer;

    @Override
    public SkuBatchStock save(SkuBatchStock skuBatchStock) {
        return skuBatchStockPOTransfer.toDO(skuBatchStockPORepository.save(skuBatchStockPOTransfer.toPO(skuBatchStock)));
    }

    @Override
    public List<SkuBatchStock> saveAll(List<SkuBatchStock> skuBatchStocks) {
        return skuBatchStockPOTransfer.toDOs(skuBatchStockPORepository.saveAll(skuBatchStockPOTransfer.toPOs(skuBatchStocks)));
    }

    @Override
    public SkuBatchStock findById(Long skuBatchStockId) {
        SkuBatchStockPO skuBatchStockPO = skuBatchStockPORepository.findById(skuBatchStockId).orElseThrow();
        return skuBatchStockPOTransfer.toDO(skuBatchStockPO);
    }

    @Override
    public List<SkuBatchStock> findAllByIds(Collection<Long> skuBatchIds) {
        List<SkuBatchStockPO> skuBatchStockPOS = skuBatchStockPORepository.findAllById(skuBatchIds);
        return skuBatchStockPOTransfer.toDOs(skuBatchStockPOS);
    }

    @Override
    public List<SkuBatchStock> findAllBySkuBatchAttributeId(Long skuBatchAttributeId) {
        List<SkuBatchStockPO> skuBatchStocks = skuBatchStockPORepository.findAllBySkuBatchAttributeId(skuBatchAttributeId);
        return skuBatchStockPOTransfer.toDOs(skuBatchStocks);
    }

    @Override
    public List<SkuBatchStock> findAllBySkuBatchAttributeIds(Collection<Long> skuBatchAttributeIds) {
        List<SkuBatchStockPO> skuBatchStocks = skuBatchStockPORepository.findAllBySkuBatchAttributeIdIn(skuBatchAttributeIds);
        return skuBatchStockPOTransfer.toDOs(skuBatchStocks);
    }

    @Override
    public SkuBatchStock findBySkuBatchAttributeIdAndWarehouseAreaId(Long skuBatchAttributeId, Long warehouseAreaId) {
        SkuBatchStockPO skuBatchStockPO = skuBatchStockPORepository
                .findBySkuBatchAttributeIdAndWarehouseAreaId(skuBatchAttributeId, warehouseAreaId);
        return skuBatchStockPOTransfer.toDO(skuBatchStockPO);
    }

    @Override
    public List<SkuBatchStock> findAllBySkuIdAndWarehouseAreaIdAndGreaterThan(Long skuId, Long warehouseAreaId, int totalQty) {
        return skuBatchStockPOTransfer.toDOs(skuBatchStockPORepository.findAllBySkuIdAndWarehouseAreaIdAndTotalQtyGreaterThan(skuId, warehouseAreaId, totalQty));
    }

    @Override
    public List<SkuBatchStock> findAllBySkuIdsAndWarehouseAreaIdAndGreaterThan(Collection<Long> skuIds, Long warehouseAreaId, int totalQty) {
        return skuBatchStockPOTransfer.toDOs(skuBatchStockPORepository.findAllBySkuIdInAndWarehouseAreaIdAndTotalQtyGreaterThan(skuIds, warehouseAreaId, totalQty));
    }

    @Override
    public List<SkuBatchStock> findAllBySkuIdInAndWarehouseAreaIdInAndTotalQtyGreaterThan(Collection<Long> skuId, Collection<Long> warehouseAreaIds, int limitTotalQty) {
        return skuBatchStockPOTransfer.toDOs(skuBatchStockPORepository.findAllBySkuIdInAndWarehouseAreaIdInAndTotalQtyGreaterThan(skuId, warehouseAreaIds, limitTotalQty));
    }

    @Override
    public Page<SkuBatchStock> findAllByPage(Collection<Long> warehouseAreaIds, PageRequest pageRequest) {
        return skuBatchStockPORepository.findAllByWarehouseAreaIdIn(warehouseAreaIds, pageRequest).map(skuBatchStockPOTransfer::toDO);
    }

    @Override
    public void clearSkuBatchStockByIds(Set<Long> skuBatchStockIds) {
        skuBatchStockPORepository.deleteAllByIdInBatch(skuBatchStockIds);
    }

    @Override
    public List<SkuBatchStock> findAllByWarehouseAreaIdInAndTotalQtyGreaterThan(Collection<Long> warehouseAreaIds, int limitTotalQty) {
        return skuBatchStockPOTransfer.toDOs(skuBatchStockPORepository.findAllByWarehouseAreaIdInAndTotalQtyGreaterThan(warehouseAreaIds, limitTotalQty));
    }

}
