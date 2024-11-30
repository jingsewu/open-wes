package org.openwes.wes.stock.application;

import static org.openwes.common.utils.constants.RedisConstants.STOCK_GET_CREATE_SKU_BATCH_ATTR_LOCK;

import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.stock.domain.entity.SkuBatchAttribute;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.SkuBatchAttributeRepository;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.domain.transfer.SkuBatchAttributeTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class SkuAttributeApiImpl implements ISkuBatchAttributeApi {

    private final SkuBatchAttributeRepository skuBatchAttributeRepository;
    private final SkuBatchStockRepository skuBatchStockRepository;
    private final SkuBatchAttributeTransfer skuBatchAttributeTransfer;
    private final DistributeLock distributeLock;

    @Override
    public List<SkuBatchAttributeDTO> getBySkuBatchStockIds(Collection<Long> skuBatchStockIds) {
        List<SkuBatchStock> skuBatchStocks = skuBatchStockRepository.findAllByIds(skuBatchStockIds);

        List<SkuBatchAttribute> skuBatchAttributes = skuBatchAttributeRepository
            .findAllByIds(skuBatchStocks.stream().map(SkuBatchStock::getSkuBatchAttributeId).toList());

        Map<Long, List<SkuBatchStock>> groupMap = skuBatchStocks.stream()
            .collect(Collectors.groupingBy(SkuBatchStock::getSkuBatchAttributeId));
        List<SkuBatchAttributeDTO> skuBatchAttributeDTOS = skuBatchAttributeTransfer.toDTOs(skuBatchAttributes);
        skuBatchAttributeDTOS.forEach(v ->
            v.setSkuBatchStockIds(groupMap.get(v.getId()).stream().map(SkuBatchStock::getId).toList()));

        return skuBatchAttributeDTOS;
    }

    @Override
    public List<SkuBatchAttributeDTO> getBySkuBatchAttributeIds(Collection<Long> skuBatchAttributeIds) {
        List<SkuBatchAttribute> skuBatchAttributes = skuBatchAttributeRepository
            .findAllByIds(skuBatchAttributeIds);
        return skuBatchAttributeTransfer.toDTOs(skuBatchAttributes);
    }

    @Override
    public SkuBatchAttributeDTO getOrCreateSkuBatchAttribute(Long skuId, Map<String, Object> batchAttributes) {

        SkuBatchAttribute newSkuBatchAttribute = new SkuBatchAttribute(skuId, batchAttributes);
        String batchNo = newSkuBatchAttribute.getBatchNo();

        distributeLock.acquireLockIfThrows(STOCK_GET_CREATE_SKU_BATCH_ATTR_LOCK + skuId + batchNo);
        try {
            SkuBatchAttribute skuBatchAttribute = skuBatchAttributeRepository.findBySkuIdAndBatchNo(skuId, batchNo);

            if (skuBatchAttribute != null) {
                return skuBatchAttributeTransfer.toDTO(skuBatchAttribute);
            }
            return skuBatchAttributeTransfer.toDTO(skuBatchAttributeRepository.save(newSkuBatchAttribute));
        } finally {
            distributeLock.releaseLock(STOCK_GET_CREATE_SKU_BATCH_ATTR_LOCK + skuId + batchNo);
        }
    }

    @Override
    public List<SkuBatchAttributeDTO> getBySkuIds(Collection<Long> skuIds) {
        List<SkuBatchAttribute> skuBatchAttributes = skuBatchAttributeRepository.findAllBySkuIds(skuIds);
        return skuBatchAttributeTransfer.toDTOs(skuBatchAttributes);
    }

}
