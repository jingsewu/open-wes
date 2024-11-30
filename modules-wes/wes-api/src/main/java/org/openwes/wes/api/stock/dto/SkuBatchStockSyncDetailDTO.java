package org.openwes.wes.api.stock.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class SkuBatchStockSyncDetailDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4726259374289805445L;
    private String ownerCode;
    private String skuCode;
    private Long qty;
    private Map<String, Object> batchAttributes;
}
