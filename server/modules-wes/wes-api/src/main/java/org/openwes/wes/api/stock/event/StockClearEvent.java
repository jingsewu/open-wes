package org.openwes.wes.api.stock.event;

import org.openwes.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockClearEvent extends DomainEvent {

    private List<Long> containerStockIds;
}
