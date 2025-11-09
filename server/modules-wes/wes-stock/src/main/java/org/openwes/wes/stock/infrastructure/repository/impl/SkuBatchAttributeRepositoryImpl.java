package org.openwes.wes.stock.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.stock.domain.entity.SkuBatchAttribute;
import org.openwes.wes.stock.domain.repository.SkuBatchAttributeRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.SkuBatchAttributePORepository;
import org.openwes.wes.stock.infrastructure.persistence.transfer.SkuBatchAttributePOTransfer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        if (skuBatchAttributeIds == null || skuBatchAttributeIds.isEmpty()) {
            return Collections.emptyList();
        }

        int batchSize = 500;

        return Lists.partition(new ArrayList<>(skuBatchAttributeIds.stream().distinct().toList()), batchSize)
                .stream()
                .map(skuBatchAttributePORepository::findAllById)
                .flatMap(List::stream)
                .map(skuBatchAttributePOTransfer::toDO)
                .collect(Collectors.toList());
    }

    @Override
    public SkuBatchAttribute findBySkuIdAndBatchNo(Long skuId, String batchNo) {
        return skuBatchAttributePOTransfer.toDO(skuBatchAttributePORepository.findBySkuIdAndBatchNo(skuId, batchNo));
    }

    @Override
    public List<SkuBatchAttribute> findAllBySkuIds(Collection<Long> skuIds) {

        if (ObjectUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }

        int batchSize = 500;

        return Lists.partition(new ArrayList<>(skuIds.stream().distinct().toList()), batchSize)
                .stream()
                .map(skuBatchAttributePORepository::findAllBySkuIdIn)
                .flatMap(List::stream)
                .map(skuBatchAttributePOTransfer::toDO)
                .collect(Collectors.toList());
    }
}
