package org.openwes.wes.outbound.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;

@Data
@Accessors(chain = true)
public class OutboundPreAllocatedRecord {

    private Long id;

    private String ownerCode;
    private Long outboundPlanOrderId;
    private Long outboundPlanOrderDetailId;
    private Collection<Long> warehouseAreaIds;

    private Long skuId;
    private Map<String, Object> batchAttributes;

    private Long skuBatchStockId;
    private Long warehouseAreaId;

    private Integer qtyPreAllocated;
    private Long version;


    public void cancel() {
        this.qtyPreAllocated = 0;
    }
}
