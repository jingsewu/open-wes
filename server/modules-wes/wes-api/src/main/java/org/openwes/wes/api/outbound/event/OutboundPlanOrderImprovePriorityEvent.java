package org.openwes.wes.api.outbound.event;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@Getter
@Setter
@Accessors(chain = true)
public class OutboundPlanOrderImprovePriorityEvent extends DomainEvent {
    @NotNull
    private Long outboundPlanOrderId;
    @NotNull
    private Integer priority;
}
