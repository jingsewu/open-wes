package org.openwes.wes.api.stock.event;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class StockAbnormalRecordCreatedEvent extends DomainEvent {

    @NotEmpty
    private String orderNo;

    public StockAbnormalRecordCreatedEvent(Long id, String orderNo) {
        super(id);
        this.orderNo = orderNo;
    }
}
