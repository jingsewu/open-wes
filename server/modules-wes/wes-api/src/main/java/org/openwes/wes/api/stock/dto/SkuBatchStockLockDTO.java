package org.openwes.wes.api.stock.dto;

import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuBatchStockLockDTO {

    @NotNull
    private Long skuBatchStockId;
    @NotNull
    private StockLockTypeEnum lockType;
    @NotNull
    private Integer lockQty;

    @Deprecated
    private Long orderDetailId;
}
