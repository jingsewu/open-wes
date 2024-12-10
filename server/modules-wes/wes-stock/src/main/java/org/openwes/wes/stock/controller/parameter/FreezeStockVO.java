package org.openwes.wes.stock.controller.parameter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FreezeStockVO {

    @NotNull
    private Long id;

    @NotNull
    @Min(1)
    private int qty;

}
