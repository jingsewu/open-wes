package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.SkuBatchAttributePO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkuBatchAttributePORepository extends JpaRepository<SkuBatchAttributePO, Long> {

    List<SkuBatchAttributePO> findAllBySkuId(Long skuId);

    SkuBatchAttributePO findBySkuIdAndBatchNo(Long skuId, String batchNo);

    List<SkuBatchAttributePO> findAllBySkuIdIn(Collection<Long> skuIds);
}
