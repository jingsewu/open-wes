package org.openwes.wes.api.algo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class PickingOrderAssignedResult {

    private Long pickingOrderId;

    private Map<Long, String> assignedStationSlot;

    private List<OperationTaskDTO> operationTasks;
}
