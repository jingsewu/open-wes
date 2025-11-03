package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OutboundPlanOrderCompleteEvent extends DomainEvent {

    public OutboundPlanOrderCompleteEvent(Long outboundPlanOrderId) {
        super(outboundPlanOrderId);
    }
}
