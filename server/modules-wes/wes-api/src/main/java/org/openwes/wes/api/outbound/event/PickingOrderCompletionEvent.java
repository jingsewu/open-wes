package org.openwes.wes.api.outbound.event;

import lombok.NoArgsConstructor;
import org.openwes.domain.event.api.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PickingOrderCompletionEvent extends DomainEvent {

    public PickingOrderCompletionEvent(Long pickingOrderId) {
        super(pickingOrderId);
    }
}
