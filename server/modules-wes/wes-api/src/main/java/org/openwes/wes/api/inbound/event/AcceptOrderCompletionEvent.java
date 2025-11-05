package org.openwes.wes.api.inbound.event;

import lombok.NoArgsConstructor;
import org.openwes.domain.event.api.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AcceptOrderCompletionEvent extends DomainEvent {
    private Long acceptOrderId;

    public AcceptOrderCompletionEvent(Long acceptOrderId) {
        super(acceptOrderId);
        this.acceptOrderId = acceptOrderId;
    }
}
