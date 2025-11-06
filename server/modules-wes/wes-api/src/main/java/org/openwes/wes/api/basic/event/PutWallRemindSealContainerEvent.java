package org.openwes.wes.api.basic.event;

import lombok.NoArgsConstructor;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.basic.dto.PutWallSlotRemindSealedDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

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

    public PutWallRemindSealContainerEvent(Long putWallSlotId,Long workStationId,
                                           String putWallSlotCode,Long pickingOrderId,String ptlTag) {
        super(putWallSlotId);
        this.pickingOrderId = pickingOrderId;
        this.putWallSlotId = putWallSlotId;
        this.putWallSlotCode = putWallSlotCode;
        this.workStationId = workStationId;
        this.ptlTag = ptlTag;
    }

}
