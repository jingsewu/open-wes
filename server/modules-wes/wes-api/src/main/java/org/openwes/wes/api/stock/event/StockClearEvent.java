package org.openwes.wes.api.stock.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StockClearEvent extends DomainEvent {
    private String containerCode;
    private String warehouseCode;
}
