package org.openwes.wes.api.basic.event;

import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.basic.dto.PutWallSlotRemindSealedDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RemindSealContainerEvent extends DomainEvent {

    private List<PutWallSlotRemindSealedDTO> details;

}
