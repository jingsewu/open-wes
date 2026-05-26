package org.openwes.wes.outbound.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openwes.common.utils.jpa.ModificationAware;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderDetailStatusEnum;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundPlanOrderDetail implements ModificationAware {

    private Long id;
    private Long outboundPlanOrderId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String ownerCode;

    private Map<String, Object> batchAttributes;

    private Integer qtyRequired;
    @Builder.Default
    private Integer qtyAllocated = 0;
    @Builder.Default
    private Integer qtyActual = 0;
    private Set<Long> warehouseAreaIds;

    private Map<String, String> extendFields;

    private OutboundPlanOrderDetailStatusEnum outboundPlanOrderDetailStatus;

    private Long version;

    private boolean modified;

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void cancel() {
        if (this.outboundPlanOrderDetailStatus != OutboundPlanOrderDetailStatusEnum.NEW) {
            throw new IllegalArgumentException("Outbound Plan Order Detail is not NEW, can not cancel");
        }
        this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.CANCELED;
        this.modified = true;
    }

    public void picking(Integer operatedQty) {
        this.qtyActual += operatedQty;
        if (this.qtyActual > this.qtyRequired) {
            throw new IllegalArgumentException("Picking quantity exceeds the required quantity");
        }
        if (this.qtyActual.equals(this.qtyAllocated)) {
            this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.PICKED;
        }
        this.modified = true;
    }

    public void shortComplete() {
        if (this.outboundPlanOrderDetailStatus == OutboundPlanOrderDetailStatusEnum.PICKED) {
            return;
        }
        this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.PICKED;
        this.modified = true;
    }

    public void preAllocate(OutboundPreAllocatedRecord planPreAllocatedRecord) {
        this.qtyAllocated += planPreAllocatedRecord.getQtyPreAllocated();

        if (this.qtyAllocated > this.qtyRequired) {
            throw new IllegalArgumentException("allocate quantity exceeds the required quantity");
        }
        this.modified = true;
    }

    public void initialize(Long outboundPlanOrderId) {
        this.outboundPlanOrderId = outboundPlanOrderId;
        this.modified = true;
    }

    public void enrichSkuInfo(Long skuId, String skuName) {
        this.skuId = skuId;
        this.skuName = skuName;
    }
}
