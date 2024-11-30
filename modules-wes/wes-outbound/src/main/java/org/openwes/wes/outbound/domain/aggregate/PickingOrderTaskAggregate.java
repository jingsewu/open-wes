package org.openwes.wes.outbound.domain.aggregate;

import com.google.common.collect.Lists;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.algo.dto.PickingOrderAssignedResult;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.dto.AssignOrdersDTO;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.ContainerStockLockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openwes.common.utils.constants.RedisConstants.NEW_PICKING_ORDER_IDS;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickingOrderTaskAggregate {

    private final ITaskApi taskApi;
    private final IStockApi stockApi;
    private final IPutWallApi putWallApi;
    private final PickingOrderRepository pickingOrderRepository;
    private final ContainerTaskApiFacade containerTaskApiFacade;
    private final RedisUtils redisUtils;

    @Transactional(rollbackFor = Exception.class)
    public void dispatchPickingOrders(List<OperationTaskDTO> operationTaskDTOS, List<PickingOrder> pickingOrders,
                                      List<PickingOrderAssignedResult> pickingOrderAssignedResults) {

        List<OperationTaskDTO> operationTasks = this.createOperationTasks(operationTaskDTOS);

        if (pickingOrderAssignedResults == null) {
            pickingOrders.forEach(v -> v.dispatch(null));
            pickingOrderRepository.saveAllOrders(pickingOrders);
            return;
        }

        Map<Long, Map<Long, String>> assignedMap = pickingOrderAssignedResults.stream()
                .collect(Collectors.toMap(PickingOrderAssignedResult::getPickingOrderId, v -> MapUtils.emptyIfNull(v.getAssignedStationSlot())));
        pickingOrders.forEach(v -> v.dispatch(assignedMap.get(v.getId())));
        pickingOrderRepository.saveAllOrders(pickingOrders);

        List<AssignOrdersDTO.AssignDetail> assignDetails = Lists.newArrayList();
        pickingOrders.forEach(v -> v.getAssignedStationSlot().forEach((key, value) -> {
            AssignOrdersDTO.AssignDetail assignDetail = new AssignOrdersDTO.AssignDetail().
                    setOrderId(v.getId()).setPutWallSlotCode(value).setWorkStationId(key);
            assignDetails.add(assignDetail);
        }));

        if (CollectionUtils.isNotEmpty(assignDetails)) {
            putWallApi.assignOrders(new AssignOrdersDTO().setAssignDetails(assignDetails));
        }

        containerTaskApiFacade.createContainerTasks(operationTasks, ContainerTaskTypeEnum.OUTBOUND);
    }

    @Transactional(rollbackFor = Exception.class)
    public void picking(List<OperationTaskPickingDTO> operationTasks, List<PickingOrder> pickingOrders) {
        pickingOrders.forEach(pickingOrder -> operationTasks.forEach(operationTaskDTO -> {
            if (Objects.equals(pickingOrder.getId(), operationTaskDTO.getOrderId())) {
                pickingOrder.picking(operationTaskDTO.getOperatedQty(), operationTaskDTO.getDetailId());
            }
        }));

        pickingOrderRepository.saveOrderAndDetails(pickingOrders);

    }

    @Transactional(rollbackFor = Exception.class)
    public void reallocate(PickingOrderReallocatedResult pickingOrderReallocatedResult, PickingOrder pickingOrder) {

        if (pickingOrderReallocatedResult.getDetails() == null) {
            log.info("picking order reallocate detail is empty");
            return;
        }

        pickingOrderReallocatedResult.getDetails().stream().flatMap(v -> v.getOperationTasks().stream())
                .forEach(operationTaskDTO -> pickingOrder.reallocateAbnormal(operationTaskDTO.getRequiredQty(), operationTaskDTO.getDetailId()));
        pickingOrderRepository.saveOrderAndDetail(pickingOrder);

        //find all shorted picking order detail
        List<PickingOrderDetail> pickingOrderDetails = pickingOrder.getDetails().stream()
                .filter(v -> v.getQtyAbnormal() > 0).toList();

        //short picked
        if (ObjectUtils.isNotEmpty(pickingOrderDetails)) {
            Set<Long> pickingOrderIds = pickingOrderDetails.stream().map(PickingOrderDetail::getPickingOrderId).collect(Collectors.toSet());
            List<PickingOrder> shortPickedPickingOrders = pickingOrderRepository.findOrderAndDetailsByPickingOrderIds(pickingOrderIds);
            pickingOrderDetails.forEach(pickingOrderDetail -> pickingOrder.shortPicking(pickingOrderDetail.getQtyAbnormal(), pickingOrderDetail.getId()));
            pickingOrderRepository.saveOrderAndDetails(shortPickedPickingOrders);
        }

        pickingOrderReallocatedResult.getDetails().forEach(detail -> {
            lockSkuBatchStocks(detail.getOperationTasks());
            // same area operation task just create operation tasks
            if (Objects.equals(detail.getWarehouseAreaId(), pickingOrder.getWarehouseAreaId())) {
                createOperationTasks(detail.getOperationTasks());
            } else {
                createPickingOrder(detail, pickingOrder);
            }
        });
    }

    private void createPickingOrder(PickingOrderReallocatedResult.PickingOrderReallocatedDetail detail, PickingOrder pickingOrder) {
        PickingOrder newPickingOrder = pickingOrder.copyAndNew(detail.getWarehouseAreaId());

        Map<Long, PickingOrderDetail> pickingOrderDetailMap = pickingOrder.getDetails().stream().collect(Collectors.toMap(PickingOrderDetail::getId, Function.identity()));

        List<PickingOrderDetail> pickingOrderDetails = detail.getOperationTasks().stream().map(operationTaskDTO -> {
            PickingOrderDetail pickingOrderDetail = pickingOrderDetailMap.get(operationTaskDTO.getDetailId());
            return pickingOrderDetail.copyAndNew(operationTaskDTO.getSkuBatchStockId(), operationTaskDTO.getRequiredQty());
        }).toList();

        newPickingOrder.setDetails(pickingOrderDetails);
        pickingOrderRepository.saveOrderAndDetail(newPickingOrder);

        String redisKey = NEW_PICKING_ORDER_IDS + "_" + newPickingOrder.getWarehouseCode();
        redisUtils.pushAll(redisKey, Lists.newArrayList(newPickingOrder.getId()));

    }

    private List<OperationTaskDTO> createOperationTasks(List<OperationTaskDTO> operationTaskDTOS) {
        List<OperationTaskDTO> savedOperationTasks = taskApi.createOperationTasks(operationTaskDTOS);

        List<ContainerStockLockDTO> lockDTOS = savedOperationTasks.stream()
                .map(v -> ContainerStockLockDTO.builder().lockType(StockLockTypeEnum.OUTBOUND)
                        .lockQty(v.getRequiredQty()).taskId(v.getId())
                        .containerStockId(v.getContainerStockId())
                        .build()).toList();
        stockApi.lockContainerStock(lockDTOS);

        return savedOperationTasks;
    }

    private void lockSkuBatchStocks(List<OperationTaskDTO> operationTasks) {
        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = Lists.newArrayList();
        operationTasks.forEach(v -> {
            SkuBatchStockLockDTO skuBatchStockLockDTO = SkuBatchStockLockDTO.builder().skuBatchStockId(v.getSkuBatchStockId())
                    .lockType(StockLockTypeEnum.OUTBOUND)
                    .lockQty(v.getRequiredQty())
                    .orderDetailId(v.getDetailId()).build();
            skuBatchStockLockDTOS.add(skuBatchStockLockDTO);
        });
        stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);
    }
}
