package org.openwes.wes.api.stocktake.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class StocktakeTaskCompletionEvent extends DomainEvent {

    @NotNull
    private Long stocktakeTaskId;

    @NotNull
    private Long stocktakeOrderId;

    public StocktakeTaskCompletionEvent(Long stocktakeTaskId, Long stockTakeOrderId) {
        super(stocktakeTaskId);
        this.stocktakeTaskId = stocktakeTaskId;
        this.stocktakeOrderId = stockTakeOrderId;
    }
}
