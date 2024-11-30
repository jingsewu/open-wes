package org.openwes.api.platform.api.dto.callback.wms;

import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "出库单拣选完成后的回调数据")
public class OutboundPickedDetailDTO {

    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "货主编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerCode;

    @Schema(title = "波次号")
    private String waveNo;

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

    @Schema(title = "优先级", description = "")
    private Integer priority;

    @Schema(title = "订单号", description = "根据系统定义的编号规则生成的订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Schema(title = "商品类别数")
    private Integer skuKindNum;

    @Schema(title = "出库总数")
    private Integer totalQty;

    @Schema(title = "出库单状态")
    private OutboundPlanOrderStatusEnum outboundPlanOrderStatus;

    @Schema(title = "是否异常")
    private boolean abnormal;

    @Schema(title = "异常原因")
    private String abnormalReason;

    @Schema(description = "扩展字段")
    private Map<String, String> reservedFields;

    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Schema(title = "商品名称")
    private String skuName;

    @Schema(description = "批次属性")
    private Map<String, Object> requestBatchAttributes;

    @Schema(title = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRequired;

    @Schema(title = "实际出库数量")
    private Integer qtyActual;
}
