package org.openwes.wes.api.basic.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.basic.dto.PutWallSlotAssignedDTO;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PutWallAssignOrderEvent extends DomainEvent {

    private List<PutWallSlotAssignedDTO> details;

    public PutWallAssignOrderEvent(Long id, List<PutWallSlotAssignedDTO> details) {
        super(id);
        this.details = details;
    }

}
