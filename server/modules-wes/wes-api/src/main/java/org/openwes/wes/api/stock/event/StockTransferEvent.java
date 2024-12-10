package org.openwes.wes.api.stock.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class StockTransferEvent extends DomainEvent {

    @NotNull
    private StockTransferDTO stockTransferDTO;

    @NotNull
    private OperationTaskTypeEnum taskType;

    public StockTransferEvent setTaskType(OperationTaskTypeEnum taskType) {
        this.taskType = taskType;
        this.stockTransferDTO.setOperationTaskType(taskType);
        return this;
    }
}
