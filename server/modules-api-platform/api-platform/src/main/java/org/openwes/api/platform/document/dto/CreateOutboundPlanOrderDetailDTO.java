package org.openwes.api.platform.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class CreateOutboundPlanOrderDetailDTO implements Serializable {

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "货主编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Size(max = 512)
    @Schema(title = "商品名称")
    private String skuName;

    @Schema(description = "批次属性")
    private Map<String, Object> batchAttributes;

    @NotNull
    @Min(1)
    @Schema(title = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRequired;
}
