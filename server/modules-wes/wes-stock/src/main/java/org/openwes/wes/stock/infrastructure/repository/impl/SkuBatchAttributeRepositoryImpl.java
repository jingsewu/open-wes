package org.openwes.wes.stock.infrastructure.repository.impl;

import org.openwes.wes.stock.domain.entity.SkuBatchAttribute;
import org.openwes.wes.stock.domain.repository.SkuBatchAttributeRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.SkuBatchAttributePORepository;
import org.openwes.wes.stock.infrastructure.persistence.transfer.SkuBatchAttributePOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuBatchAttributeRepositoryImpl implements SkuBatchAttributeRepository {

    private final SkuBatchAttributePORepository skuBatchAttributePORepository;
    private final SkuBatchAttributePOTransfer skuBatchAttributePOTransfer;

    @Override
    public SkuBatchAttribute save(SkuBatchAttribute skuBatchAttribute) {
        return skuBatchAttributePOTransfer.toDO(skuBatchAttributePORepository.save(skuBatchAttributePOTransfer.toPO(skuBatchAttribute)));
    }

    @Override
    public List<SkuBatchAttribute> findAllBySkuId(Long skuId) {
        return skuBatchAttributePOTransfer.toDOs(skuBatchAttributePORepository.findAllBySkuId(skuId));
    }

    @Override
    public List<SkuBatchAttribute> findAllByIds(Collection<Long> skuBatchAttributeIds) {
        return skuBatchAttributePOTransfer.toDOs(skuBatchAttributePORepository.findAllById(skuBatchAttributeIds));
    }

    @Override
    public SkuBatchAttribute findBySkuIdAndBatchNo(Long skuId, String batchNo) {
        return skuBatchAttributePOTransfer.toDO(skuBatchAttributePORepository.findBySkuIdAndBatchNo(skuId, batchNo));
    }

    @Override
    public List<SkuBatchAttribute> findAllBySkuIds(Collection<Long> skuIds) {
        return skuBatchAttributePOTransfer.toDOs(skuBatchAttributePORepository.findAllBySkuIdIn(skuIds));
    }
}
