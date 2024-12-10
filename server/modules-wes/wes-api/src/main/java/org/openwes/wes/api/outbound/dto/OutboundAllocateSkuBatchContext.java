package org.openwes.wes.api.outbound.dto;

import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Data
@Accessors(chain = true)
public class OutboundAllocateSkuBatchContext {

    private Map<Long, String> skuCategoryMap;
    private Map<Pair<String, String>, BatchAttributeConfigDTO> skuBatchAttributeConfigMap;
    private Map<Long, List<SkuBatchAttributeDTO>> skuBatchAttributeMap;
    private Map<Long, WarehouseAreaDTO> warehouseAreaMap;
    private List<SkuBatchStockDTO> skuBatchStocks;

    public List<SkuBatchStockDTO> matchSkuBatchStocks(Long skuId, String ownerCode, Map<String, Object> batchAttributes) {

        List<SkuBatchAttributeDTO> skuBatchAttributeDTOs = this.skuBatchAttributeMap.get(skuId);
        if (CollectionUtils.isEmpty(skuBatchAttributeDTOs)) {
            return Collections.emptyList();
        }

        String skuFirstCategory = this.skuCategoryMap.get(skuId);

        BatchAttributeConfigDTO batchAttributeConfigDTO = this.skuBatchAttributeConfigMap.get(Pair.of(ownerCode, skuFirstCategory));
        if (batchAttributeConfigDTO != null) {
            skuBatchAttributeDTOs = skuBatchAttributeDTOs.stream().filter(v -> v.match(batchAttributeConfigDTO, batchAttributes)).toList();
        }

        Set<Long> skuBatchAttributeIds = skuBatchAttributeDTOs.stream().map(SkuBatchAttributeDTO::getId).collect(Collectors.toSet());
        List<SkuBatchStockDTO> filteredSkuBatchStockDTOs = skuBatchStocks.stream().filter(v -> skuBatchAttributeIds.contains(v.getSkuBatchAttributeId())).toList();

        Map<Long, SkuBatchAttributeDTO> skuBatchAttributeDTOMap = skuBatchAttributeDTOs.stream()
                .collect(Collectors.toMap(SkuBatchAttributeDTO::getId, Function.identity()));

        filteredSkuBatchStockDTOs.forEach(skuBatchStock -> skuBatchStock.initCompareData(warehouseAreaMap.get(skuBatchStock.getWarehouseAreaId()),
                skuBatchAttributeDTOMap.get(skuBatchStock.getSkuBatchAttributeId())));
        return filteredSkuBatchStockDTOs.stream().sorted().toList();
    }
}
