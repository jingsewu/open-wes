package org.openwes.wes.api.basic.event;

import lombok.NoArgsConstructor;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PutWallRemindSealContainerEvent extends DomainEvent {

    private Long putWallSlotId;
    private Long workStationId;
    private String putWallSlotCode;
    private Long pickingOrderId;
    private String ptlTag;
    private PutWallSlotStatusEnum putWallSlotStatus;

    public PutWallRemindSealContainerEvent(Long putWallSlotId, Long workStationId,
                                           String putWallSlotCode, Long pickingOrderId, String ptlTag) {
        super(putWallSlotId);
        this.pickingOrderId = pickingOrderId;
        this.putWallSlotId = putWallSlotId;
        this.putWallSlotCode = putWallSlotCode;
        this.workStationId = workStationId;
        this.ptlTag = ptlTag;
    }

}
