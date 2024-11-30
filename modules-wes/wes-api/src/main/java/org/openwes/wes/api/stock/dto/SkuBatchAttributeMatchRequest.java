package org.openwes.wes.api.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class SkuBatchAttributeMatchRequest {

    private Long identifyId;
    private Long skuId;
    private Map<String, Object> batchAttributes;

}
