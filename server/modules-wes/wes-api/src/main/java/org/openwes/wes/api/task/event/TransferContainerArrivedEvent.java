package org.openwes.wes.api.task.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class TransferContainerArrivedEvent extends DomainEvent {

    @NotNull
    private Long warehouseAreaId;

    @NotEmpty
    private List<TransferContainerArriveDetail> details;

    @Accessors(chain = true)
    @Data
    public static class TransferContainerArriveDetail {
        @NotEmpty
        private String containerCode;
        @NotEmpty
        private String locationCode;
    }
}
