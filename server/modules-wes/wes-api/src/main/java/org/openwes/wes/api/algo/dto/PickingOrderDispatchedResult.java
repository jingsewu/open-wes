package org.openwes.wes.api.algo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@Slf4j
public class PickingOrderDispatchedResult {

    private List<PickingOrderAssignedResult> assignedResults;

    public void validate(PickingOrderHandlerContext pickingOrderHandlerContext) {

        Map<Long, PickingOrderDTO> pickingOrderDTOMap = pickingOrderHandlerContext.getPickingOrders()
                .stream().collect(Collectors.toMap(PickingOrderDTO::getId, v -> v));

        assignedResults = assignedResults.stream()
                .filter(assignedResult -> {
                    PickingOrderDTO pickingOrderDTO = pickingOrderDTOMap.get(assignedResult.getPickingOrderId());
                    boolean fulfilled = orderRequirementFullFilled(pickingOrderDTO, assignedResult.getOperationTasks());
                    if (!fulfilled) {
                        log.error("picking order: {} requirement not full filled. then remove the result", assignedResult.getPickingOrderId());
                    }
                    return fulfilled;
                })
                .collect(Collectors.toList());
    }

    private boolean orderRequirementFullFilled(PickingOrderDTO pickingOrderDTO, List<OperationTaskDTO> pickingOrderTaskDTOS) {
        return pickingOrderDTO.getDetails().stream().collect(Collectors.groupingBy(PickingOrderDTO.PickingOrderDetailDTO::getSkuBatchStockId))
                .entrySet().stream().allMatch(entry -> {
                    Long skuBatchStockId = entry.getKey();
                    List<PickingOrderDTO.PickingOrderDetailDTO> details = entry.getValue();
                    int totalRequired = details.stream().mapToInt(PickingOrderDTO.PickingOrderDetailDTO::getQtyRequired).sum();
                    int totalAlgoResult = pickingOrderTaskDTOS.stream()
                            .filter(task -> task.getSkuBatchStockId().equals(skuBatchStockId))
                            .mapToInt(OperationTaskDTO::getRequiredQty).sum();
                    if (totalAlgoResult != totalRequired) {
                        log.error("picking order: {} skuBatchStockId: {} algo result: {} not equals requirement: {}",
                                pickingOrderDTO.getId(), skuBatchStockId, totalAlgoResult, totalRequired);
                    }
                    return totalAlgoResult == totalRequired;
                });

    }
}
