package org.openwes.wes.algo.outbound.domain.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocateContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface StockAllocationService {

    Stream<OperationTaskDTO> allocateSinglePickingOrder(PickingOrderDTO pickingOrder, PickingOrderHandlerContext pickingOrderHandlerContext);

    default Stream<OperationTaskDTO> allocate(PickingOrderDTO pickingOrder, List<ContainerStockDTO> containerStocks,
                                              Map<Long, Set<String>> stationAllocatedContainers) {

        Map<Long, List<ContainerStockDTO>> skuBatchStockMap = containerStocks
                .stream().collect(Collectors.groupingBy(ContainerStockDTO::getSkuBatchStockId));

        return pickingOrder.getDetails().stream().filter(v -> v.getQtyRequired() > 0).flatMap(detail -> {
            List<ContainerStockDTO> containerStockDTOS = skuBatchStockMap.get(detail.getSkuBatchStockId());
            if (CollectionUtils.isEmpty(containerStockDTOS)) {
                return null;
            }

            return generateOperationTasks(pickingOrder, stationAllocatedContainers, detail, containerStockDTOS);
        }).filter(Objects::nonNull);
    }

    private static Stream<OperationTaskDTO> generateOperationTasks(PickingOrderDTO pickingOrder, Map<Long, Set<String>> stationAllocatedContainers,
                                                                   PickingOrderDTO.PickingOrderDetailDTO detail, List<ContainerStockDTO> containerStockDTOS) {
        AtomicInteger qtyRequired = new AtomicInteger(detail.getQtyRequired());
        return containerStockDTOS.stream().filter(v -> v.getAvailableQty() > 0).flatMap(containerStockDTO -> {
            if (qtyRequired.get() <= 0) {
                return null;
            }
            int qtyPreAllocated = Math.min(qtyRequired.get(), containerStockDTO.getAvailableQty());

            qtyRequired.addAndGet(-qtyPreAllocated);

            containerStockDTO.setAvailableQty(containerStockDTO.getAvailableQty() - qtyPreAllocated);

            //cache session allocate container
            if (MapUtils.isNotEmpty(pickingOrder.getAssignedStationSlot())) {
                pickingOrder.getAssignedStationSlot().forEach((workStationId, putWallSlotCode) -> {
                    if (stationAllocatedContainers.containsKey(workStationId)) {
                        stationAllocatedContainers.get(workStationId).add(containerStockDTO.getContainerCode());
                    } else {
                        stationAllocatedContainers.put(workStationId, Sets.newHashSet(containerStockDTO.getContainerCode()));
                    }
                });
            }

            return Stream.of(new OperationTaskDTO()
                    .setPriority(pickingOrder.getPriority())
                    .setBoxNo(containerStockDTO.getBoxNo())
                    .setSkuId(containerStockDTO.getSkuId())
                    .setSkuBatchStockId(containerStockDTO.getSkuBatchStockId())
                    .setSkuBatchAttributeId(containerStockDTO.getSkuBatchAttributeId())
                    .setSourceContainerCode(containerStockDTO.getContainerCode())
                    .setSourceContainerFace(containerStockDTO.getContainerFace())
                    .setSourceContainerSlot(containerStockDTO.getContainerSlotCode())
                    .setAssignedStationSlot(pickingOrder.getAssignedStationSlot())
                    .setContainerStockId(containerStockDTO.getId())
                    .setRequiredQty(qtyPreAllocated)
                    .setOperatedQty(0)
                    .setAbnormalQty(0)
                    .setTaskType(OperationTaskTypeEnum.PICKING)
                    .setWarehouseCode(pickingOrder.getWarehouseCode())
                    .setOrderId(pickingOrder.getId())
                    .setOrderNo(pickingOrder.getPickingOrderNo())
                    .setDetailId(detail.getId()));
        });
    }

    default PickingOrderReallocatedResult reallocateSinglePickingOrder(PickingOrderReallocateContext pickingOrderReallocateContext) {

        List<OperationTaskDTO> operationTaskDTOS = pickingOrderReallocateContext.getPickingOrder().getDetails().stream().filter(v -> v.getQtyAbnormal() > 0)
                .flatMap(detail -> {
                    PickingOrderReallocateContext.PickingOrderReallocateDetail pickingOrderReallocateDetail = pickingOrderReallocateContext.getPickingOrderReallocateDetail(detail.getId());

                    List<ContainerStockDTO> containerStocks = pickingOrderReallocateDetail.getSortedContainerStocks();
                    return generateOperationTasks(pickingOrderReallocateContext.getPickingOrder(), Maps.newHashMap(), detail, containerStocks);
                }).toList();

        List<PickingOrderReallocatedResult.PickingOrderReallocatedDetail> details = Lists.newArrayList();
        Map<Long, List<OperationTaskDTO>> warehouseAreaOperationTaskMap = operationTaskDTOS.stream()
                .collect(Collectors.groupingBy(v -> pickingOrderReallocateContext.getWarehouseAreaId(v.getSkuBatchStockId())));

        warehouseAreaOperationTaskMap.forEach((warehouseAreaId, operationTasks) ->
                details.add(new PickingOrderReallocatedResult.PickingOrderReallocatedDetail()
                        .setOperationTasks(operationTasks)
                        .setWarehouseAreaId(warehouseAreaId)));

        return new PickingOrderReallocatedResult().setDetails(details);
    }
}
