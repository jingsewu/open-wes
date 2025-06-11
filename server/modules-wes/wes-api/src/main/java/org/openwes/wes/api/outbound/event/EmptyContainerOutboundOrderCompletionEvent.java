package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class EmptyContainerOutboundOrderCompletionEvent extends DomainEvent {

    private List<Long> emptyContainerOutboundOrderDetailIds;
}
