package org.openwes.wes.api.inbound.dto;

import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.common.utils.utils.ObjectUtils;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "入库计划单")
@EqualsAndHashCode(callSuper = true)
public class InboundPlanOrderDTO extends UpdateUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1718569161397991562L;

    @Schema(title = "订单 ID")
    private Long id;

    @Size(max = 64)
    @Schema(title = "订单号", description = "根据系统定义的编号规则生成的订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "客户订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderNo;

    @Size(max = 64)
    @Schema(title = "LPN 编码")
    private String lpnCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "入库计划单类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderType;

    @NotNull
    @Schema(title = "存储类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private StorageTypeEnum storageType;

    @Hidden
    private boolean abnormal;

    @Size(max = 128)
    @Schema(title = "发货人")
    private String sender;

    @Size(max = 128)
    @Schema(title = "物流公司")
    private String carrier;

    @Size(max = 128)
    @Schema(title = "购买方式")
    private String shippingMethod;

    @Size(max = 128)
    @Schema(title = "快递单号")
    private String trackingNumber;

    @Schema(title = "预计送达日期")
    private Long estimatedArrivalDate;

    @Size(max = 255)
    @Schema(title = "备注")
    private String remark;

    /**
     * SKU 种类编号
     */
    private Integer skuKindNum;

    private Integer totalQty = 0;
    private Integer totalBox = 0;

    @Schema(title = "状态")
    private InboundPlanOrderStatusEnum inboundPlanOrderStatus;

    @Schema(description = "扩展字段")
    private Map<String, Object> extendFields;

    @Hidden
    private Long version;

    @NotEmpty
    @Schema(description = "入库计划单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<InboundPlanOrderDetailDTO> details;

    public static void main(String[] args) {
        System.out.println(JsonUtils.obj2String(ObjectUtils.getRandomObject(InboundPlanOrderDTO.class)));
    }
}
