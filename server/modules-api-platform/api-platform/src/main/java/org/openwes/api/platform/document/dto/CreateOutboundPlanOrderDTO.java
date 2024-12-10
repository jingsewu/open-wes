package org.openwes.api.platform.document.dto;

import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "出库计划单")
public class CreateOutboundPlanOrderDTO implements Serializable {

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "波次号")
    private String waveNo;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "客户订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderNo;

    @Schema(title = "出库单类型")
    private String customerOrderType;

    @Schema(title = "承运人编码")
    private String currierCode;

    @Schema(title = "运单号")
    private String waybillNo;

    @Schema(title = "原始平台编码")
    private String origPlatformCode;

    @Schema(title = "截单时间")
    private Long expiredTime;

    @Schema(title = "优先级", description = "越大优先级越高")
    private Integer priority;

    @Schema(title = "是否允许短出", description = "默认 false 不允许")
    private boolean shortOutbound = false;

    @Schema(description = "扩展字段")
    private Map<String, String> reservedFields;

    @NotEmpty
    @Schema(description = "出库计划单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OutboundPlanOrderDTO.OutboundPlanOrderDetailDTO> details;
}
