package org.openwes.wes.outbound.domain.entity;

import org.openwes.wes.api.outbound.constants.OutboundPlanOrderDetailStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

@Data
@Accessors(chain = true)
public class OutboundPlanOrderDetail {

    private Long id;
    private Long outboundPlanOrderId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String ownerCode;

    private Map<String, Object> batchAttributes;

    private Integer qtyRequired;
    private Integer qtyAllocated = 0;
    private Integer qtyActual = 0;
    private Set<Long> warehouseAreaIds;

    private Map<String, String> reservedFields;

    private OutboundPlanOrderDetailStatusEnum outboundPlanOrderDetailStatus;

    private Long version;

    public void cancel() {
        if (this.outboundPlanOrderDetailStatus != OutboundPlanOrderDetailStatusEnum.NEW) {
            throw new IllegalArgumentException("Outbound Plan Order Detail is not NEW, can not cancel");
        }
        this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.CANCELED;
    }

    public void picking(Integer operatedQty) {
        this.qtyActual += operatedQty;
        if (this.qtyActual > this.qtyRequired) {
            throw new IllegalArgumentException("Picking quantity exceeds the required quantity");
        }
        if (this.qtyActual.equals(this.qtyAllocated)) {
            this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.PICKED;
        }
    }

    public void shortComplete() {
        this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.PICKED;
    }

    public void preAllocate(OutboundPreAllocatedRecord planPreAllocatedRecord) {
        this.qtyAllocated += planPreAllocatedRecord.getQtyPreAllocated();

        if (this.qtyAllocated > this.qtyRequired) {
            throw new IllegalArgumentException("allocate quantity exceeds the required quantity");
        }
    }
}
