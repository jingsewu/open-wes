package org.openwes.wes.api.task.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class OperationTaskPickedEvent extends DomainEvent {

    @NotNull
    private OperationTaskPickingDTO operationTaskPicking;

    public OperationTaskPickedEvent(Long operationTaskId, OperationTaskPickingDTO operationTaskPicking) {
        super(operationTaskId);
        this.operationTaskPicking = operationTaskPicking;
    }

}
