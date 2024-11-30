package org.openwes.wes.api.task.event;

import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PickingOrderPickingEvent extends DomainEvent {

    @NotEmpty
    private List<OperationTaskPickingDTO> operationTasks;
}
