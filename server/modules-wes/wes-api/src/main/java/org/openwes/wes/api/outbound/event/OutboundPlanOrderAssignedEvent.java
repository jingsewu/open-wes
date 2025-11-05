package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class OutboundPlanOrderAssignedEvent extends DomainEvent {

    private String warehouseCode;

    public OutboundPlanOrderAssignedEvent(Long outboundPlanOrderId, String warehouseCode) {
        super(outboundPlanOrderId);
        this.warehouseCode = warehouseCode;
    }
}
