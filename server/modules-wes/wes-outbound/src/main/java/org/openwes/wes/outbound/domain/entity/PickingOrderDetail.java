package org.openwes.wes.outbound.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openwes.common.utils.jpa.ModificationAware;
import org.openwes.wes.api.outbound.constants.PickingOrderDetailStatusEnum;

import java.util.Collection;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickingOrderDetail implements ModificationAware {

    private Long id;
    private String ownerCode;
    @Setter(AccessLevel.PACKAGE)
    private Long pickingOrderId;
    private Long outboundOrderPlanDetailId;
    private Long outboundOrderPlanId;
    private Long skuId;
    private Map<String, Object> batchAttributes;

    private Long skuBatchStockId;

    private Integer qtyRequired;
    private Integer qtyActual;
    private Collection<Long> retargetingWarehouseAreaIds;

    private Integer qtyAbnormal;
    private Integer qtyShort;

    private PickingOrderDetailStatusEnum pickingOrderDetailStatus;

    private Long version;

    private boolean modified;

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void cancel() {
        if (this.pickingOrderDetailStatus != PickingOrderDetailStatusEnum.NEW) {
            throw new IllegalStateException("picking order details status is not NEW, can't be canceled");
        }
        this.pickingOrderDetailStatus = PickingOrderDetailStatusEnum.CANCELED;
        this.modified = true;
    }

    public void picking(Integer operatedQty) {
        this.qtyActual += operatedQty;
        if (this.qtyActual + this.qtyShort > this.qtyRequired) {
            throw new IllegalArgumentException("Picking quantity + short quantity exceeds the required quantity");
        }
        if (this.qtyActual + qtyShort == this.qtyRequired) {
            this.pickingOrderDetailStatus = PickingOrderDetailStatusEnum.PICKED;
        }
        this.modified = true;
    }

    public void reportAbnormal(Integer abnormalQty) {
        this.qtyAbnormal += abnormalQty;

        if (this.qtyAbnormal + this.qtyActual + this.qtyShort > this.qtyRequired) {
            throw new IllegalArgumentException("abnormal quantity exceeds the required quantity");
        }

        this.modified = true;
    }

    public void reallocateAbnormal(Integer allocatedQty) {
        this.qtyAbnormal -= allocatedQty;

        if (this.qtyAbnormal < 0) {
            throw new IllegalArgumentException("abnormal quantity is less than zero");
        }

        this.modified = true;
    }

    public void shortPicking(Integer shortQty) {
        this.qtyAbnormal -= shortQty;
        this.qtyShort += shortQty;
        if (this.qtyAbnormal != 0) {
            throw new IllegalArgumentException("abnormal quantity isn't zero");
        }
        if (this.qtyActual + this.qtyShort > this.qtyRequired) {
            throw new IllegalArgumentException("picking quantity exceeds the required quantity");
        }
        if (this.qtyActual + qtyShort == this.qtyRequired) {
            this.pickingOrderDetailStatus = PickingOrderDetailStatusEnum.PICKED;
        }

        this.modified = true;
    }

    public PickingOrderDetail copyAndNew(Long skuBatchStockId, Integer requiredQty) {
        return PickingOrderDetail.builder()
                .ownerCode(this.ownerCode)
                .pickingOrderId(this.pickingOrderId)
                .outboundOrderPlanDetailId(this.outboundOrderPlanDetailId)
                .outboundOrderPlanId(this.outboundOrderPlanId)
                .skuId(this.skuId)
                .batchAttributes(this.batchAttributes)
                .skuBatchStockId(skuBatchStockId)
                .qtyRequired(requiredQty)
                .qtyActual(0)
                .qtyShort(0)
                .qtyAbnormal(0)
                .retargetingWarehouseAreaIds(this.retargetingWarehouseAreaIds)
                .pickingOrderDetailStatus(this.pickingOrderDetailStatus)
                .modified(true)
                .build();
    }
}
