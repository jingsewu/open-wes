package org.openwes.wes.algo.outbound.domain.service.impl;

import org.openwes.wes.algo.outbound.domain.entity.OrderAssignStationModel;
import org.openwes.wes.algo.outbound.domain.service.RobotAreaOrderAssignService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RobotAreaOrderAssignServiceImpl implements RobotAreaOrderAssignService {

    @Override
    public void matchingAvailablePutWallSlots(PickingOrderDTO pickingOrderDTO, List<PutWallSlotDTO> putWallSlots) {

        List<PickingOrderDTO.AvailablePutWallSlot> availablePutWallSlots = putWallSlots.stream().map(putWallSlot ->
                        new PickingOrderDTO.AvailablePutWallSlot()
                                .setPutWallSlotCode(putWallSlot.getPutWallSlotCode())
                                .setWorkStationId(putWallSlot.getWorkStationId()))
                .toList();
        pickingOrderDTO.setAvailablePutWallSlots(availablePutWallSlots);
    }


    // calculate order and station matching
    //1. whether required sku in the work station picking tasks
    //2. whether required sku in the picking tasks containers
    //3. control station balance: sku amount balance
    @Override
    public long calculateStationScore(Long workStationId, OrderAssignStationModel orderAssignStationModel,
                                      List<PickingOrderDTO.PickingOrderDetailDTO> details) {

        long skuBatchCount = details.stream().filter(detail ->
                orderAssignStationModel.getStationOperationSkuBatches().getOrDefault(workStationId, Collections.emptySet())
                        .contains(detail.getSkuBatchStockId())).distinct().count();

        Set<ContainerStockDTO> currentStationContainStocks = orderAssignStationModel.getStationContainerStocks()
                .getOrDefault(workStationId, Collections.emptySet());
        Set<Long> requiredSkuBatchIdSet = details.stream().map(PickingOrderDTO.PickingOrderDetailDTO::getSkuBatchStockId)
                .collect(Collectors.toSet());
        long containerCount = currentStationContainStocks.stream()
                .filter(stockDTO -> requiredSkuBatchIdSet.contains(stockDTO.getSkuBatchStockId()))
                .map(ContainerStockDTO::getContainerCode).distinct().count();

        int undoPickingSkuAmount = orderAssignStationModel.getStationOperationTasks().getOrDefault(workStationId, Collections.emptySet())
                .stream().map(OperationTaskDTO::getRequiredQty).reduce(Integer::sum).orElse(0);

        return skuBatchCount * 100 + containerCount * 80 + undoPickingSkuAmount * 5L;
    }

    @Override
    public void assign(PickingOrderDTO pickingOrderDTO, Long workStationId, List<PutWallSlotDTO> availablePutWallSlots) {

        PickingOrderDTO.AvailablePutWallSlot firstAvailablePutWallSlot = pickingOrderDTO.getAvailablePutWallSlots()
                .stream().filter(v -> v.getWorkStationId().equals(workStationId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No available put wall slot find."));

        availablePutWallSlots.stream().filter(v -> v.getWorkStationId().equals(firstAvailablePutWallSlot.getWorkStationId())
                        && v.getPutWallSlotCode().equals(firstAvailablePutWallSlot.getPutWallSlotCode())).findFirst()
                .ifPresent(v -> {
                    if (v.getPutWallSlotStatus() != PutWallSlotStatusEnum.IDLE) {
                        return;
                    }
                    pickingOrderDTO.setAssignedStationSlot(Map.of(workStationId, v.getPutWallSlotCode()));
                    v.setPutWallSlotStatus(PutWallSlotStatusEnum.WAITING_BINDING);
                });

    }
}
