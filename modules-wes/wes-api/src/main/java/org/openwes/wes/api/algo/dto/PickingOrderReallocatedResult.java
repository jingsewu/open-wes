package org.openwes.wes.api.algo.dto;

import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PickingOrderReallocatedResult {

    private List<PickingOrderReallocatedDetail> details;

    @Data
    @Accessors(chain = true)
    public static class PickingOrderReallocatedDetail {
        private Long warehouseAreaId;
        private List<OperationTaskDTO> operationTasks;
    }
}
