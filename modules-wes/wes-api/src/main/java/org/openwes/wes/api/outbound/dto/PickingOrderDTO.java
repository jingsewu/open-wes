package org.openwes.wes.api.outbound.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderTaskTypeEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "拣货单信息")
public class PickingOrderDTO implements Serializable {

    @Hidden
    private Long id;

    @Schema(title = "仓库编码")
    private String warehouseCode;

    @Schema(title = "仓区编号")
    private Long warehouseAreaId;

    @Schema(title = "波次号")
    private String waveNo;

    @Schema(title = "拣货单编号")
    private String pickingOrderNo;

    @Schema(title = "优先级")
    private int priority;

    @Schema(title = "是否允许短出")
    private boolean shortOutbound;

    @Hidden
    private PickingOrderStatusEnum pickingOrderStatus;

    @Hidden
    private PickingOrderTaskTypeEnum pickingOrderTaskType;

    /**
     * one picking order can be assigned to multiple station slot
     * <p>
     * Key is the station id
     * Value is the put wall slot code
     */
    @Schema(description = "任务分配情况")
    private Map<Long, String> assignedStationSlot;

    @Schema(description = "已领操作员账号")
    private String receivedUserAccount;

    @Schema(description = "是否允许领用")
    private boolean allowReceive;

    @Schema(description = "拣货单明细列表")
    private List<PickingOrderDetailDTO> details;

    /**
     * used when calculating the available put wall slots that the order can be assigned
     */
    @Hidden
    private List<AvailablePutWallSlot> availablePutWallSlots;

    @Hidden
    private Long version;

    @Data
    public static class PickingOrderDetailDTO implements Serializable {

        @Hidden
        private Long id;

        private String ownerCode;

        @Hidden
        private Long pickingOrderId;

        @Hidden
        private Long outboundOrderPlanId;

        @Hidden
        private Long outboundOrderPlanDetailId;

        @Schema(title = "商品 id")
        private Long skuId;

        @Schema(title = "批次信息")
        private Map<String, Object> batchAttributes;

        @Hidden
        private Long skuBatchStockId;

        @Schema(title = "需捡数量")
        private Integer qtyRequired;

        @Schema(title = "实际捡数量")
        private Integer qtyActual;

        @Schema(title = "异常数量")
        private Integer qtyAbnormal;

        @Schema(title = "短拣数量")
        private Integer qtyShort;

        @Hidden
        private Long version;

    }

    @Data
    @Accessors(chain = true)
    public static class AvailablePutWallSlot {
        private Long workStationId;
        private String putWallSlotCode;
    }
}
