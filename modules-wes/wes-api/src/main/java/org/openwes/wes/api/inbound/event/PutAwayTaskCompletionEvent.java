package org.openwes.wes.api.inbound.event;

import org.openwes.domain.event.DomainEvent;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PutAwayTaskCompletionEvent extends DomainEvent implements Serializable {

    @NotEmpty
    private List<PutAwayTaskCompleteDetail> details;

    @Data
    @Accessors(chain = true)
    public static class PutAwayTaskCompleteDetail {

        @NotEmpty
        private Long putAwayTaskId;

        @NotEmpty
        private String locationCode;
        private Long warehouseAreaId;
    }
}
