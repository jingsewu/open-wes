package org.openwes.wes.outbound.application.scheduler;

import com.alibaba.ttl.TtlRunnable;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.distribute.lock.DistributeLock;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final DistributeLock distributeLock;

    private final Executor pickingOrderHandleExecutor;
    private final Executor pickingOrderReallocateExecutor;

    private static final int MAX_SIZE_PER_TIME = 1000;

    @Scheduled(cron = "*/5 * * * * *")
    public void pickingOrderHandle() {
        List<String> keys = redisUtils.keys(RedisUtils.generateKeysPatten("", NEW_PICKING_ORDER_IDS));
        keys.forEach(key -> {
            List<Long> pickingOrderIds = redisUtils.getListByPureKey(key, MAX_SIZE_PER_TIME);
            if (CollectionUtils.isEmpty(pickingOrderIds)) {
                return;
            }

            CompletableFuture.runAsync(Objects.requireNonNull(TtlRunnable.get(() ->
                            this.tryHandlePickingOrders(pickingOrderIds, key))), pickingOrderHandleExecutor)
                    .exceptionally(e -> {
                        log.error("handle picking order failed.", e);
                        return null;
                    });
        });

    }

    private void tryHandlePickingOrders(List<Long> pickingOrderIds, String key) {
        boolean acquireLock = distributeLock.acquireLock(RedisConstants.PICKING_ORDER_DISPATCH_SCHEDULE_EXECUTE_LOCK, 0);
        if (!acquireLock) {
            return;
        }
        try {
            String warehouseCode = key.substring(key.lastIndexOf("_") + 1);
            List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderAndDetailsByPickingOrderIds(pickingOrderIds)
                    .stream().filter(v -> v.getPickingOrderStatus() == PickingOrderStatusEnum.NEW).toList();
            if (CollectionUtils.isEmpty(pickingOrders)) {
                log.warn("can not find new picking orders, may be short completed, picking order ids : {}", pickingOrderIds);
                redisUtils.removeListByPureKey(key, pickingOrderIds);
                return;
            }

            handlePickingOrders(pickingOrders, warehouseCode, key);
        } finally {
            distributeLock.releaseLock(RedisConstants.PICKING_ORDER_DISPATCH_SCHEDULE_EXECUTE_LOCK);
        }
    }

    private void handlePickingOrders(List<PickingOrder> pickingOrders, String warehouseCode, String key) {

        List<Long> warehouseAreaIds = pickingOrders.stream().map(PickingOrder::getWarehouseAreaId).toList();
        Map<Long, WarehouseAreaDTO> warehouseAreaMap = warehouseAreaApi.getByIds(warehouseAreaIds).stream()
                .collect(Collectors.toMap(WarehouseAreaDTO::getId, Function.identity()));

        Map<WarehouseAreaWorkTypeEnum, List<PickingOrder>> pickingOrderMap = pickingOrders.stream()
                .collect(Collectors.groupingBy(v -> warehouseAreaMap.get(v.getWarehouseAreaId()).getWarehouseAreaWorkType()));

        List<PickingOrder> robotPickingOrders = pickingOrderMap.get(WarehouseAreaWorkTypeEnum.ROBOT);
        if (CollectionUtils.isNotEmpty(robotPickingOrders)) {
            handleRobotAreaPickingOrders(robotPickingOrders, warehouseCode, key);
        }

        List<PickingOrder> manualPickingOrders = pickingOrderMap.get(WarehouseAreaWorkTypeEnum.MANUAL);
        if (CollectionUtils.isNotEmpty(manualPickingOrders)) {
            handleManualAreaPickingOrders(manualPickingOrders, warehouseCode, key);
        }
    }

    private void handleRobotAreaPickingOrders(List<PickingOrder> robotPickingOrders, String warehouseCode, String key) {

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
        List<OperationTaskDTO> operationTaskDTOS = pickingOrderDispatchedResult.getOperationTaskDTOS();

        List<PickingOrder> assignedPickingOrders = robotPickingOrders.stream()
                .filter(v -> pickingOrderAssignedResults.stream().anyMatch(r -> r.getPickingOrderId().equals(v.getId()))).toList();
        pickingOrderTaskAggregate.dispatchPickingOrders(operationTaskDTOS, assignedPickingOrders, pickingOrderAssignedResults);

        redisUtils.removeListByPureKey(key, assignedPickingOrders.stream().map(PickingOrder::getId).toList());
    }

    private void handleManualAreaPickingOrders(List<PickingOrder> manualPickingOrders, String warehouseCode, String key) {

        PickingOrderHandlerContext pickingOrderHandlerContext = pickingOrderService.prepareStockContext(warehouseCode, manualPickingOrders);

        List<OperationTaskDTO> operationTaskDTOS = pickingOrderService.allocateStocks(pickingOrderHandlerContext);

        pickingOrderTaskAggregate.dispatchPickingOrders(operationTaskDTOS, manualPickingOrders, null);

        redisUtils.removeListByPureKey(key, manualPickingOrders.stream().map(PickingOrder::getId).toList());
    }


    @Scheduled(cron = "0 0/5 * * * *")
    public void handleAbnormalOrders() {

        List<Long> abnormalTaskIds = redisUtils.getList(RedisConstants.PICKING_ORDER_ABNORMAL_DETAIL_IDS);
        if (CollectionUtils.isEmpty(abnormalTaskIds)) {
            return;
        }

        CompletableFuture
                .runAsync(Objects.requireNonNull(TtlRunnable.get(()
                        -> this.reallocate(abnormalTaskIds))), pickingOrderReallocateExecutor)
                .exceptionally(e -> {
                    log.error("reallocate failed", e);
                    return null;
                });
    }

    private void reallocate(List<Long> pickingOrderDetailIds) {
        boolean acquireLock = distributeLock.acquireLock(RedisConstants.PICKING_ORDER_REALLOCATE_LOCK, 0);
        if (acquireLock) {
            log.warn("picking order reallocate can't get distribute lock: {}", RedisConstants.PICKING_ORDER_REALLOCATE_LOCK);
            return;
        }
        try {
            pickingOrderApi.reallocate(pickingOrderDetailIds);
        } finally {
            distributeLock.releaseLock(RedisConstants.PICKING_ORDER_REALLOCATE_LOCK);
        }
    }

}
