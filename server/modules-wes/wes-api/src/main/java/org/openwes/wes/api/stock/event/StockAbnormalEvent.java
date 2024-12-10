package org.openwes.wes.api.stock.event;

import org.openwes.domain.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAbnormalEvent extends DomainEvent {

    private List<Long> stockAbnormalRecordIds;
}
