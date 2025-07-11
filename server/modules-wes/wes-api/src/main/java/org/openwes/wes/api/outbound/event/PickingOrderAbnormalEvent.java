package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PickingOrderAbnormalEvent extends DomainEvent {

    private List<PickingOrderAbnormalDetail> details;

    @Data
    @Accessors(chain = true)
    public static class PickingOrderAbnormalDetail {
        private Long pickingOrderId;
        private Long pickingOrderDetailId;
        private Integer abnormalQty;
    }
}
