package org.openwes.station.application.business.handler.event.stocktake;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "盘点盈品事件")
public class StocktakeSurplusSkuEvent implements Serializable {
    @NotNull
    private Long skuId;

    /**
     * 批次属性id（原批次盈品）
     */
    private Long skuBatchAttributeId;

    /**
     * 批次属性（新批次盈品）
     */
    private Map<String, Object> skuAttributes;

    @NotEmpty
    private String containerCode;

    @NotEmpty
    private String containerFace;

    @NotEmpty
    private String containerSlotCode;

    @Min(1)
    private Integer stocktakeQty;
}
