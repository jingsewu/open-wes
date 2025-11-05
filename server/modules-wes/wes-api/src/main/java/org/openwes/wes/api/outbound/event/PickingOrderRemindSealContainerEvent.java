package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PickingOrderRemindSealContainerEvent extends DomainEvent {

    private Long warehouseAreaId;
    private Map<Long, String> assignedStationSlots;

    public PickingOrderRemindSealContainerEvent(Long pickingOrderId, Long warehouseAreaId, Map<Long, String> assignedStationSlots) {
        super(pickingOrderId);
        this.warehouseAreaId = warehouseAreaId;
        this.assignedStationSlots = assignedStationSlots;
    }
}
