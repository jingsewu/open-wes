package org.openwes.wes.api.basic.event;

import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.basic.dto.PutWallSlotAssignedDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AssignOrderEvent extends DomainEvent {

    private List<PutWallSlotAssignedDTO> details;

}
