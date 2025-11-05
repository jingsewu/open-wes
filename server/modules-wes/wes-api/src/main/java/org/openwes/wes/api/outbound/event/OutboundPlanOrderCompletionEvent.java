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
public class OutboundPlanOrderCompletionEvent extends DomainEvent {

    public OutboundPlanOrderCompletionEvent(Long outboundPlanOrderId) {
        super(outboundPlanOrderId);
    }
}
