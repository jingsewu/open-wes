package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "盘点单明细")
public class StocktakeOrderDetailDTO implements Serializable {

    @Schema(title = "盘点单明细 id")
    private Long id;

    @Schema(title = "盘点单 id")
    private Long stocktakeOrderId;

    @Schema(title = "容器编码")
    private String containerCode;

    @Schema(title = "库存ID")
    private Long stockId;

    @Schema(title = "库存数量")
    private Integer qtyOriginal;

    @Schema(title = "盘点数量")
    private Integer qtyStocktake;

    @Schema(title = "SKU ID")
    private Long skuId;

    @Hidden
    private Long version;
}
