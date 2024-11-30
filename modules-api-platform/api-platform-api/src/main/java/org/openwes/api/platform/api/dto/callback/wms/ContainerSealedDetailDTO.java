package org.openwes.api.platform.api.dto.callback.wms;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Data
@Schema(description = "出库封箱完成后回调数据明细")
public class ContainerSealedDetailDTO implements Serializable {

    @Schema(title = "库区 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long warehouseAreaId;

    @Schema(title = "工作站编号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long workStationId;

    @Schema(title = "操作员")
    private String operator;

    @Schema(title = "播种墙格口编号")
    private String putWallSlotCode;

    @Schema(title = "目的地")
    private Set<String> destinations;

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

    @Schema(description = "扩展字段")
    private Map<String, String> reservedFields;

    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Schema(title = "商品名称")
    private String skuName;

    @Schema(description = "批次属性")
    private Map<String, Object> batchAttributes;

    @Schema(title = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRequired;

    @Schema(title = "实际出库数量")
    private Integer qtyActual;
}
