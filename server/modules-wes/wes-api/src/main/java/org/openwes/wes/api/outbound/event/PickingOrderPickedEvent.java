package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PickingOrderPickedEvent extends DomainEvent {

    private PickingDetail pickingDetail;

    public PickingOrderPickedEvent(Long pickingOrderId, PickingDetail detail) {
        super(pickingOrderId);
        this.pickingDetail = detail;
    }

    @Data
    @Accessors(chain = true)
    public static class PickingDetail implements Serializable {
        private Long outboundOrderDetailId;
        private Long outboundOrderId;
        private Integer operatedQty;
    }
}
