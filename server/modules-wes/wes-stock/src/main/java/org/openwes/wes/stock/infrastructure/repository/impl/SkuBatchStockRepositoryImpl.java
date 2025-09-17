package org.openwes.wes.stock.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.SkuBatchStockPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.SkuBatchStockPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.SkuBatchStockPOTransfer;
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
    public void clearSkuBatchStockByIds(Set<Long> skuBatchStockIds) {
        skuBatchStockPORepository.deleteAllByIdInBatch(skuBatchStockIds);
    }

    @Override
    public void deleteAllZeroQtyStock(long expiredTime) {
        skuBatchStockPORepository.deleteAllByUpdateTimeBeforeAndTotalQty(expiredTime, 0);
    }

}
