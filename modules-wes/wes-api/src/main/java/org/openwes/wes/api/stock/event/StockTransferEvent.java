package org.openwes.wes.api.stock.event;

import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferEvent extends DomainEvent {

    @NotNull
    private StockTransferDTO stockTransferDTO;

    @NotNull
    private OperationTaskTypeEnum taskType;

    public void setTaskType(OperationTaskTypeEnum taskType) {
        this.taskType = taskType;
        stockTransferDTO.setOperationTaskType(taskType);
    }
}
