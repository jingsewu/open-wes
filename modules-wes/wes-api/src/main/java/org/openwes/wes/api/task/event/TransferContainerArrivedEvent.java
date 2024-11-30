package org.openwes.wes.api.task.event;

import org.openwes.domain.event.DomainEvent;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
public class TransferContainerArrivedEvent extends DomainEvent {

    @NotEmpty
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
