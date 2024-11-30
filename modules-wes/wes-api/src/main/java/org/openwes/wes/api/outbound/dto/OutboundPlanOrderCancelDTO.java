package org.openwes.wes.api.outbound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OutboundPlanOrderCancelDTO implements Serializable {


    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @NotEmpty
    @Schema(title = "客户订单号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> customerOrderNos;

    @Schema(title = "同步取消波次下的所有的订单", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean syncCancelOrdersInSameWave;

}
