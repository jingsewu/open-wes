package org.openwes.wes.api.basic.event;

import org.openwes.domain.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ContainerStockUpdateEvent extends DomainEvent {
    @Serial
    private static final long serialVersionUID = -2857350432988211380L;

    private String containerCode;
    private String warehouseCode;
}
