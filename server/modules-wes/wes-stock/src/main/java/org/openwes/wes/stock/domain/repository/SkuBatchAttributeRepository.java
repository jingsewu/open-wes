package org.openwes.wes.stock.domain.repository;

import org.openwes.wes.stock.domain.entity.SkuBatchAttribute;

import java.util.Collection;
import java.util.List;

public interface SkuBatchAttributeRepository {

    SkuBatchAttribute save(SkuBatchAttribute skuBatchAttribute);

    List<SkuBatchAttribute> findAllBySkuId(Long skuId);

    List<SkuBatchAttribute> findAllByIds(Collection<Long> skuBatchAttributeIds);

    SkuBatchAttribute findBySkuIdAndBatchNo(Long skuId, String batchNo);

    List<SkuBatchAttribute> findAllBySkuIds(Collection<Long> skuIds);
}
