package org.openwes.wes.api.algo.dto;

import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class PickingOrderDispatchedResult {

    private List<PickingOrderAssignedResult> assignedResults;

    private List<OperationTaskDTO> operationTaskDTOS;

    public void validate(PickingOrderHandlerContext pickingOrderHandlerContext) {

        if (ObjectUtils.isEmpty(operationTaskDTOS)) {
            return;
        }

        Map<Long, PickingOrderDTO> pickingOrderDTOMap = pickingOrderHandlerContext.getPickingOrders().stream().collect(Collectors.toMap(PickingOrderDTO::getId, v -> v));

        operationTaskDTOS.stream().collect(Collectors.groupingBy(OperationTaskDTO::getOrderId))
                .forEach((pickingOrderId, pickingOrderTaskDTOS) -> {
                    PickingOrderDTO pickingOrderDTO = pickingOrderDTOMap.get(pickingOrderId);
                    if (!orderRequirementFullFilled(pickingOrderDTO, pickingOrderTaskDTOS)) {
                        throw new IllegalStateException("order requirement not full filled");
                    }
                });

    }

    private boolean orderRequirementFullFilled(PickingOrderDTO pickingOrderDTO, List<OperationTaskDTO> pickingOrderTaskDTOS) {
        return pickingOrderDTO.getDetails().stream().collect(Collectors.groupingBy(PickingOrderDTO.PickingOrderDetailDTO::getSkuBatchStockId))
                .entrySet().stream().allMatch(entry -> {
                    Long skuBatchStockId = entry.getKey();
                    List<PickingOrderDTO.PickingOrderDetailDTO> details = entry.getValue();
                    int totalRequired = details.stream().mapToInt(PickingOrderDTO.PickingOrderDetailDTO::getQtyRequired).sum();
                    int totalAlgoResult = pickingOrderTaskDTOS.stream().filter(task -> task.getSkuBatchStockId().equals(skuBatchStockId))
                            .mapToInt(OperationTaskDTO::getRequiredQty).sum();
                    return totalAlgoResult == totalRequired;

                });

    }
}
