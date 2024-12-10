package org.openwes.wes.api.stock;

import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ISkuBatchAttributeApi {

    List<SkuBatchAttributeDTO> getBySkuBatchStockIds(@NotEmpty Collection<Long> skuBatchStockIds);

    List<SkuBatchAttributeDTO> getBySkuBatchAttributeIds(@NotEmpty Collection<Long> skuBatchAttributeIds);

    SkuBatchAttributeDTO getOrCreateSkuBatchAttribute(@NotNull Long skuId, Map<String, Object> batchAttributes);

    List<SkuBatchAttributeDTO> getBySkuIds(@NotEmpty Collection<Long> skuIds);
}
