package org.openwes.wes.outbound.domain.entity;

import org.openwes.common.utils.jpa.ModificationAware;
import org.openwes.wes.api.outbound.constants.PickingOrderDetailStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.Map;

@Data
@Accessors(chain = true)
public class PickingOrderDetail implements ModificationAware {

    private Long id;
    private String ownerCode;
    private Long pickingOrderId;
    private Long outboundOrderPlanDetailId;
    private Long outboundOrderPlanId;
    private Long skuId;
    private Map<String, Object> batchAttributes;

    private Long skuBatchStockId;

    private Integer qtyRequired;
    private Integer qtyActual;
    private Collection<Long> retargetingWarehouseAreaIds;

    //表示上报的异常数量
    private Integer qtyAbnormal;
    //表示当前任务短拣的数量，当异常数量重新命中后，说明当前任务短拣了，则需要增加此数量
    private Integer qtyShort;

    //当且仅当 this.shortQty + this.operatedQty == this.requiredQty 时,明细完成
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
        PickingOrderDetail newPickingOrderDetail = new PickingOrderDetail();
        BeanUtils.copyProperties(this, newPickingOrderDetail, "id");
        newPickingOrderDetail.setSkuBatchStockId(skuBatchStockId);
        newPickingOrderDetail.setQtyRequired(requiredQty);
        newPickingOrderDetail.setQtyActual(0);
        newPickingOrderDetail.setQtyShort(0);
        newPickingOrderDetail.setQtyAbnormal(0);
        newPickingOrderDetail.setModified(true);
        return newPickingOrderDetail;
    }
}
