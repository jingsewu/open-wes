package org.openwes.wes.api.task.event;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OperationTaskPickedEvent extends DomainEvent {

    @NotEmpty
    private OperationTaskPickingDTO operationTaskPicking;

    public OperationTaskPickedEvent(Long operationTaskId, OperationTaskPickingDTO operationTaskPicking) {
        super(operationTaskId);
        this.operationTaskPicking = operationTaskPicking;
    }

}
