package org.openwes.wes.api.algo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class PickingOrderAssignedResult {

    private Long pickingOrderId;

    @NotNull
    private Map<Long, String> assignedStationSlot;

    private List<OperationTaskDTO> operationTasks;
}
