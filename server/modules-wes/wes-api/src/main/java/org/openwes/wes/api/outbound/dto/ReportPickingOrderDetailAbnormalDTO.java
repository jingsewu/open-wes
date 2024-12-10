package org.openwes.wes.api.outbound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "拣货单明细异常上报")
public class ReportPickingOrderDetailAbnormalDTO {

    private Long pickingOrderId;

    private Long pickingOrderDetailId;

    private Integer abnormalQty;
}
