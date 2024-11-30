package org.openwes.wes.api.inbound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "入库计划单明细")
public class InboundPlanOrderDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2528370556344393516L;

    @Schema(title = "订单明细 ID")
    private Long id;

    @Schema(title = "订单 ID")
    private Long inboundPlanOrderId;

    @NotEmpty
    @Schema(title = "货主")
    private String ownerCode;

    @Size(max = 64)
    @Schema(title = "容器编码")
    private String containerCode;

    @Size(max = 64)
    @Schema(title = "容器规格")
    private String containerSpecCode;

    @Size(max = 64)
    @Schema(title = "格口号")
    private String containerSlotCode;

    @Size(max = 64)
    @Schema(title = "箱号")
    private String boxNo;

    @NotNull
    @Min(1)
    @Schema(title = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRestocked;

    @Schema(title = "已接收数量")
    private Integer qtyAccepted;

    @Schema(title = "异常数量")
    private Integer qtyAbnormal;

    @Size(max = 128)
    @Schema(title = "异常原因")
    private String abnormalReason;

    @Size(max = 128)
    @Schema(title = "责任方")
    private String responsibleParty;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Schema(title = "商品 ID")
    private Long skuId;

    @Size(max = 128)
    @Schema(title = "商品名称")
    private String skuName;

    @Schema(title = "商品样式")
    private String style;

    @Schema(title = "商品颜色")
    private String color;

    @Schema(title = "商品尺寸")
    private String size;

    @Schema(title = "商品品牌")
    private String brand;

    @Schema(description = "批次属性")
    private Map<String, Object> batchAttributes;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendFields;
}
