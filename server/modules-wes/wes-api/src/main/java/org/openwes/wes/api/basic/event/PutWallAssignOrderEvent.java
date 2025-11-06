package org.openwes.wes.api.basic.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PutWallAssignOrderEvent extends DomainEvent {

    public PutWallAssignOrderEvent(Long putWallSlotId, Long pickingOrderId, String putWallCode,
                                   Long workStationId, String putWallSlotCode, String ptlTag) {
        super(putWallSlotId);
        this.putWallSlotId = putWallSlotId;
        this.pickingOrderId = pickingOrderId;
        this.putWallCode = putWallCode;
        this.workStationId = workStationId;
        this.putWallSlotCode = putWallSlotCode;
        this.ptlTag = ptlTag;
    }

    private Long putWallSlotId;
    private Long pickingOrderId;
    private String putWallCode;
    private Long workStationId;
    private String putWallSlotCode;
    private String ptlTag;

}
