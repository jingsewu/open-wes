package org.openwes.wes.api.stock.event;

import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCreateEvent extends DomainEvent {

    @Serial
    private static final long serialVersionUID = -8883557627440637364L;

    @NotNull
    private StockCreateDTO stockCreateDTO;
}
