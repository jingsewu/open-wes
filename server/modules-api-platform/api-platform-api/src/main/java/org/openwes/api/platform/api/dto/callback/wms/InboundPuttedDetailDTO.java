package org.openwes.api.platform.api.dto.callback.wms;

import org.openwes.wes.api.basic.constants.LocationStatusEnum;
import org.openwes.wes.api.basic.constants.LocationTypeEnum;
import org.openwes.wes.api.inbound.constants.InboundOrderTypeEnum;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "上架完成后回传给上游的入库信息")
public class InboundPuttedDetailDTO {

    @Schema(title = "库位编码")
    private String locationCode;

    @Schema(title = "巷道编码")
    private String aisleCode;

    @Schema(title = "货架编码")
    private String shelfCode;

    @Schema(title = "库位类型")
    private LocationTypeEnum locationType;

    @Schema(title = "库位热度")
    private String heat;

    @Schema(title = "是否被占用")
    private boolean occupied;

    @Schema(title = "库位状态")
    private LocationStatusEnum locationStatus;

    @Schema(title = "订单号", description = "根据系统定义的编号规则生成的订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Schema(title = "客户订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderNo;

    @Schema(title = "LPN 编码")
    private String lpnCode;

    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "货主编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerCode;

    @Schema(title = "入库计划单类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private InboundOrderTypeEnum inboundOrderType;

    @Schema(title = "存储类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private StorageTypeEnum storageType;

    @Schema(title = "是否异常", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean abnormal;

    @Schema(title = "发货人")
    private String sender;

    @Schema(title = "物流公司")
    private String carrier;

    @Schema(title = "购买方式")
    private String shippingMethod;

    @Schema(title = "快递单号")
    private String trackingNumber;

    @Schema(title = "预计送达日期")
    private Long estimatedArrivalDate;

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "状态")
    private InboundPlanOrderStatusEnum inboundPlanOrderStatus;

    @Schema(description = "订单的扩展字段")
    private Map<String, Object> orderExtendFields;

    @Schema(title = "订单 ID")
    private Long inboundPlanOrderId;

    @Schema(title = "容器编码")
    private String containerCode;

    @Schema(title = "容器规格")
    private String containerSpecCode;

    @Schema(title = "格口号")
    private String containerSlotCode;

    @Schema(title = "箱号")
    private String boxNo;

    @Schema(title = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRestocked;

    @Schema(title = "已接收数量")
    private Integer qtyAccepted;

    @Schema(title = "未接收数量")
    private Integer qtyUnreceived;

    @Schema(title = "异常数量")
    private Integer qtyAbnormal;

    @Schema(title = "异常原因")
    private String abnormalReason;

    @Schema(title = "责任方")
    private String responsibleParty;

    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Schema(title = "商品 ID")
    private Long skuId;

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

    @Schema(description = "明细扩展属性")
    private Map<String, Object> detailExtendFields;
}
