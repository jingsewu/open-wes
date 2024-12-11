package org.openwes.wes.api.stock.event;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StockAdjustmentOrderCreatedEvent extends DomainEvent {

    @NotEmpty
    private String orderNo;
}
