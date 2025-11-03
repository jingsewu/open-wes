package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PickingOrderDispatchedEvent extends DomainEvent {

    public PickingOrderDispatchedEvent(Long pickingOrderId) {
        super(pickingOrderId);
    }
}
