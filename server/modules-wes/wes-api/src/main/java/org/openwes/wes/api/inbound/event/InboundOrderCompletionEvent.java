package org.openwes.wes.api.inbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class InboundOrderCompletionEvent extends DomainEvent {
    private Long inboundOrderId;
    public InboundOrderCompletionEvent(Long inboundOrderId) {
        super(inboundOrderId);
        this.inboundOrderId = inboundOrderId;
    }
}
