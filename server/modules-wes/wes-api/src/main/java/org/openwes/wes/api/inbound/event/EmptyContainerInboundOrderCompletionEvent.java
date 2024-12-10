package org.openwes.wes.api.inbound.event;

import org.openwes.domain.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class EmptyContainerInboundOrderCompletionEvent extends DomainEvent {

    private List<Long> emptyContainerInboundOrderDetailIds;
}
