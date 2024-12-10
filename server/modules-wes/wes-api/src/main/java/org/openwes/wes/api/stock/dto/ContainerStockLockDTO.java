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
public class ContainerStockLockDTO {
    @NotNull
    private Long containerStockId;
    private StockLockTypeEnum lockType;
    private Integer lockQty;
    private Long taskId;
}
