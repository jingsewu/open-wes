package org.openwes.wes.api.stock.dto;

import lombok.Data;

import java.util.List;

@Data
public class SkuBatchAttributeMatchResult {
    private Long identifyId;
    private List<SkuBatchAttributeDTO> skuBatchAttributes;
}
