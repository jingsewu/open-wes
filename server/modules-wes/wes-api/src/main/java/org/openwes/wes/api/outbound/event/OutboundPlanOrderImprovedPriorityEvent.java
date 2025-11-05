package org.openwes.wes.api.outbound.event;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class OutboundPlanOrderImprovedPriorityEvent extends DomainEvent {
    @NotNull
    private Integer priority;

    public OutboundPlanOrderImprovedPriorityEvent(Long outboundPlanOrderId, Integer priority) {
        super(outboundPlanOrderId);
        this.priority = priority;
    }
}
