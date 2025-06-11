package org.openwes.wes.api.task.event;

import org.openwes.domain.event.api.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TransferContainerSealedEvent extends DomainEvent {
    private Long transferContainerRecordId;
    private String warehouseCode;
}
