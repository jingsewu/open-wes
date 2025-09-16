package org.openwes.wes.api.outbound.event;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@Getter
@Setter
@Accessors(chain = true)
public class PickingOrderImprovePriorityEvent extends DomainEvent {

    @NotNull
    private Long pickingOrderId;
    @NotNull
    private Integer priority;
}
