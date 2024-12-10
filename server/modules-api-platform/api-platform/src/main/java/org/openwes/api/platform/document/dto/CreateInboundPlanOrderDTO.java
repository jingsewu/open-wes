package org.openwes.api.platform.document.dto;

import org.openwes.common.utils.validate.IValidate;
import org.openwes.common.utils.validate.ValidObject;
import org.openwes.wes.api.inbound.constants.InboundOrderTypeEnum;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@ValidObject
@Schema(description = "入库计划单")
public class CreateInboundPlanOrderDTO implements IValidate, Serializable {

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

    @NotNull
    @Schema(title = "入库计划单类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private InboundOrderTypeEnum inboundOrderType;

    @NotNull
    @Schema(title = "存储类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private StorageTypeEnum storageType;

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

    @Schema(description = "扩展字段")
    private Map<String, Object> extendFields;

    @NotEmpty
    @Schema(description = "入库计划单明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<InboundPlanOrderDetailDTO> inboundPlanOrderDetails;

    @Override
    public boolean validate() {

        Map<String, List<InboundPlanOrderDetailDTO>> uniqueDetailMap = inboundPlanOrderDetails.stream()
                .collect(Collectors.groupingBy(this::uniqueDetail));

        return uniqueDetailMap.entrySet().stream().allMatch(entry -> entry.getValue().size() == 1);
    }

    private String uniqueDetail(InboundPlanOrderDetailDTO detail) {
        return (detail.getBoxNo() == null ? "" : detail.getBoxNo()) + "$$" + detail.getSkuCode()
                + "$$" + (detail.getBatchAttributes() == null ? "" : detail.getBatchAttributes().toString());
    }
}
