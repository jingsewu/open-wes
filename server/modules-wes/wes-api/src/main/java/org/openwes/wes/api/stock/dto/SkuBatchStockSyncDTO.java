package org.openwes.wes.api.stock.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SkuBatchStockSyncDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8743102667382821681L;

    private Integer pageIndex;
    private Integer pageSize;
    private Integer pageTotal;
    private String warehouseCode;
    private List<SkuBatchStockSyncDetailDTO> details;

}
