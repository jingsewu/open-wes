package org.openwes.wes.outbound.application.scheduler;

import com.alibaba.ttl.TtlRunnable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.distribute.scheduler.annotation.DistributedScheduled;
import org.openwes.wes.api.algo.dto.PickingOrderAssignedResult;
import org.openwes.wes.api.algo.dto.PickingOrderDispatchedResult;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.outbound.domain.aggregate.PickingOrderTaskAggregate;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openwes.common.utils.constants.RedisConstants.NEW_PICKING_ORDER_IDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickingOrderHandleScheduler {

    private final IPickingOrderApi pickingOrderApi;
    private final IWarehouseAreaApi warehouseAreaApi;
    private final RedisUtils redisUtils;
    private final PickingOrderTaskAggregate pickingOrderTaskAggregate;
    private final PickingOrderRepository pickingOrderRepository;
    private final PickingOrderService pickingOrderService;

    private final Executor pickingOrderReallocateExecutor;

    private static final int MAX_SIZE_PER_TIME = 1000;

    @DistributedScheduled(fixedDelayString = "300000", name = "PickingOrderHandleScheduler#updateNewPickingOrderIds",
            lockAtLeastFor = "60s")
    public void refreshNewPickingOrderIds() {
        log.debug("schedule refresh new picking order ids.");
        List<String> keys = redisUtils.keys(RedisUtils.generateKeysPatten("", NEW_PICKING_ORDER_IDS));
        if (CollectionUtils.isNotEmpty(keys)) {
            return;
        }
        List<Long> ids = pickingOrderRepository.findAllIdsByStatus(PickingOrderStatusEnum.NEW);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        redisUtils.pushAll(RedisConstants.NEW_PICKING_ORDER_IDS, ids);
    }

    @DistributedScheduled(fixedDelayString = "10000", name = "PickingOrderHandleScheduler#pickingOrderHandle",
            lockAtLeastFor = "9s")
    public void pickingOrderHandle() {
        log.debug("schedule start execute picking order handler.");

        List<Long> pickingOrderIds = redisUtils.getList(NEW_PICKING_ORDER_IDS, MAX_SIZE_PER_TIME);
        if (CollectionUtils.isEmpty(pickingOrderIds)) {
            return;
        }

        try {
            this.tryHandlePickingOrders(pickingOrderIds);
        } catch (Exception e) {
            log.error("picking order handle error", e);
        }
    }

    private void tryHandlePickingOrders(List<Long> pickingOrderIds) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderAndDetailsByPickingOrderIds(pickingOrderIds)
                .stream().filter(v -> v.getPickingOrderStatus() == PickingOrderStatusEnum.NEW).toList();
        if (CollectionUtils.isEmpty(pickingOrders)) {
            log.warn("can not find new picking orders, may be short completed, picking order ids : {}", pickingOrderIds);
            redisUtils.removeList(NEW_PICKING_ORDER_IDS, pickingOrderIds);
            return;
        }

        pickingOrders.stream().collect(Collectors.groupingBy(PickingOrder::getWarehouseCode))
                .forEach((warehouseCode, subPickingOrders) -> {
                    handlePickingOrders(subPickingOrders, warehouseCode);
                });
    }

    private void handlePickingOrders(List<PickingOrder> pickingOrders, String warehouseCode) {

        List<Long> warehouseAreaIds = pickingOrders.stream().map(PickingOrder::getWarehouseAreaId).distinct().toList();
        Map<Long, WarehouseAreaDTO> warehouseAreaMap = warehouseAreaApi.getByIds(warehouseAreaIds).stream()
                .collect(Collectors.toMap(WarehouseAreaDTO::getId, Function.identity()));

        Map<WarehouseAreaWorkTypeEnum, List<PickingOrder>> pickingOrderMap = pickingOrders.stream()
                .collect(Collectors.groupingBy(v -> warehouseAreaMap.get(v.getWarehouseAreaId()).getWarehouseAreaWorkType()));

        List<PickingOrder> robotPickingOrders = pickingOrderMap.get(WarehouseAreaWorkTypeEnum.ROBOT);
        if (CollectionUtils.isNotEmpty(robotPickingOrders)) {
            handleRobotAreaPickingOrders(robotPickingOrders, warehouseCode);
        }

        List<PickingOrder> manualPickingOrders = pickingOrderMap.get(WarehouseAreaWorkTypeEnum.MANUAL);
        if (CollectionUtils.isNotEmpty(manualPickingOrders)) {
            handleManualAreaPickingOrders(manualPickingOrders, warehouseCode);
        }
    }

    private void handleRobotAreaPickingOrders(List<PickingOrder> robotPickingOrders, String warehouseCode) {

        PickingOrderHandlerContext pickingOrderHandlerContext = pickingOrderService.prepareFullContext(warehouseCode, robotPickingOrders);
        if (pickingOrderHandlerContext == null) {
            return;
        }
        PickingOrderDispatchedResult pickingOrderDispatchedResult = pickingOrderService.dispatchOrders(pickingOrderHandlerContext);

        if (pickingOrderDispatchedResult == null || CollectionUtils.isEmpty(pickingOrderDispatchedResult.getAssignedResults())) {
            log.info("picking orders can't be assigned, maybe there are not put wall slot rules matched");
            return;
        }

        List<PickingOrderAssignedResult> pickingOrderAssignedResults = pickingOrderDispatchedResult.getAssignedResults();
        Map<Long, PickingOrder> assignedPickingOrders = robotPickingOrders.stream().collect(Collectors.toMap(PickingOrder::getId, Function.identity()));

        pickingOrderAssignedResults.forEach(pickingOrderAssignedResult -> {
            pickingOrderTaskAggregate.dispatchPickingOrders(pickingOrderAssignedResult.getOperationTasks(),
                    assignedPickingOrders.get(pickingOrderAssignedResult.getPickingOrderId()), pickingOrderAssignedResult.getAssignedStationSlot());
            redisUtils.removeList(RedisConstants.NEW_PICKING_ORDER_IDS, Lists.newArrayList(pickingOrderAssignedResult.getPickingOrderId()));
        });

    }

    private void handleManualAreaPickingOrders(List<PickingOrder> manualPickingOrders, String warehouseCode) {

        PickingOrderHandlerContext pickingOrderHandlerContext = pickingOrderService.prepareStockContext(warehouseCode, manualPickingOrders);
        List<OperationTaskDTO> operationTaskDTOS = pickingOrderService.allocateStocks(pickingOrderHandlerContext);

        for (PickingOrder manualPickingOrder : manualPickingOrders) {
            List<OperationTaskDTO> operationTasks = operationTaskDTOS.stream()
                    .filter(v -> v.getOrderId().equals(manualPickingOrder.getId())).toList();
            pickingOrderTaskAggregate.dispatchPickingOrders(operationTasks, manualPickingOrder, null);
            redisUtils.removeList(NEW_PICKING_ORDER_IDS, Lists.newArrayList(manualPickingOrder.getId()));
        }

    }

    @DistributedScheduled(cron = "0 0/5 * * * *", name = "PickingOrderHandleScheduler#handleAbnormalOrders")
    public void handleAbnormalOrders() {

        List<Long> pickingOrderDetailIds = redisUtils.getList(RedisConstants.PICKING_ORDER_ABNORMAL_DETAIL_IDS);
        if (CollectionUtils.isEmpty(pickingOrderDetailIds)) {
            return;
        }

        CompletableFuture
                .runAsync(Objects.requireNonNull(TtlRunnable.get(()
                        -> pickingOrderApi.reallocate(pickingOrderDetailIds))), pickingOrderReallocateExecutor)
                .exceptionally(e -> {
                    log.error("reallocate failed", e);
                    return null;
                });
    }
}
