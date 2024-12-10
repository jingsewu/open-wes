package org.openwes.wes.api.stocktake.event;

import org.openwes.domain.event.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class StocktakeCancelContainerTaskEvent extends DomainEvent {
    private List<String> cancelContainerTaskCodeList;
}
