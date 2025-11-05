package org.openwes.wes.api.task.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;


@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TransferContainerSealedEvent extends DomainEvent {
    private String warehouseCode;

    public TransferContainerSealedEvent(Long transferContainerRecordId,
                                        String warehouseCode) {
        super(transferContainerRecordId);
        this.warehouseCode = warehouseCode;
    }
}
