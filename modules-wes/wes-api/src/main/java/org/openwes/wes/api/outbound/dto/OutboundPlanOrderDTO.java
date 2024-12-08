package org.openwes.wes.api.outbound.dto;

import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.common.utils.utils.ObjectUtils;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "出库计划单")
public class OutboundPlanOrderDTO implements Serializable {

    @Hidden
    @Schema(title = "出库单ID")
    private Long id;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "外部波次号")
    private String customerWaveNo;

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

    @Schema(title = "是否允许缺货等待", description = "默认 false 不允许")
    private boolean shortWaiting = false;

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

    @Valid
    @NotEmpty
    @Schema(description = "出库计划单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OutboundPlanOrderDetailDTO> details;

    @Hidden
    private Long version;

    @Data
    public static class OutboundPlanOrderDetailDTO implements Serializable {

        @NotEmpty
        @Size(max = 64)
        @Schema(title = "货主编码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String ownerCode;

        @NotEmpty
        @Size(max = 64)
        @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String skuCode;


        @Size(max = 128)
        @Schema(title = "商品名称")
        private String skuName;

        @Schema(description = "批次属性")
        private Map<String, Object> batchAttributes;

        @NotNull
        @Min(1)
        @Schema(title = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer qtyRequired;

        @Schema(title = "预占的批次库存数量")
        private Integer qtyAllocated;

        @Schema(title = "实际出库数量")
        private Integer qtyActual;
//
//        @Schema(title = "库区列表")
//        private Collection<Long> warehouseAreaIds;
    }

    public static void main(String[] args) {
        System.out.println(JsonUtils.obj2String(ObjectUtils.getRandomObject(OutboundPlanOrderDTO.class)));
    }
}
