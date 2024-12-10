package org.openwes.wes.api.outbound.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PickingOrderReallocateDTO implements Serializable {

    private Long pickingOrderId;
    private Long pickingOrderDetailId;
    private Long reallocatedWarehouseAreaId;
    private int reallocatedQty;

    public PickingOrderReallocateDTO(Long pickingOrderId, Long pickingOrderDetailId, Long reallocatedWarehouseAreaId, Integer reallocatedQty) {
        this.pickingOrderId = pickingOrderId;
        this.pickingOrderDetailId = pickingOrderDetailId;
        this.reallocatedWarehouseAreaId = reallocatedWarehouseAreaId;
        this.reallocatedQty = reallocatedQty;
    }
}
