package org.openwes.wes.api.outbound.event;

import org.openwes.domain.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OutboundPlanOrderPickingEvent extends DomainEvent {

    private List<PickingDetail> pickingDetails;

    @Data
    @Accessors(chain = true)
    public static class PickingDetail implements Serializable {
        private Long outboundOrderDetailId;
        private Long outboundOrderId;
        private Integer operatedQty;
    }
}
