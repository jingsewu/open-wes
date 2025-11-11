package org.openwes.wes.outbound.application.scheduler;

import com.alibaba.ttl.TtlRunnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.distribute.scheduler.annotation.DistributedScheduled;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.outbound.domain.aggregate.OutboundWaveAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.service.OutboundWaveService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.openwes.common.utils.constants.RedisConstants.OUTBOUND_PLAN_ORDER_ASSIGNED_IDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundWaveScheduler {

    private final RedisUtils redisUtils;
    private final OutboundWaveService outboundWaveService;
    private final OutboundWaveAggregate outboundWaveAggregate;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final Executor wavePickingExecutor;

    @DistributedScheduled(cron = "${wms.schedule.config.wavePicking:0 0/1 * * * *}",
            name = "OutboundWaveScheduler#wavePicking", lockAtLeastFor = "30s")
    public void wavePicking() {
        List<String> keys = redisUtils.keys(RedisUtils.generateKeysPatten("", OUTBOUND_PLAN_ORDER_ASSIGNED_IDS));
        keys.forEach(warehouseCode -> {
            List<Long> orderIds = redisUtils.getListByPureKey(warehouseCode);
            if (CollectionUtils.isEmpty(orderIds)) {
                return;
            }

            CompletableFuture
                    .runAsync(Objects.requireNonNull(TtlRunnable.get(()
                            -> this.wavePickingSingleWarehouse(orderIds, warehouseCode))), wavePickingExecutor)
                    .exceptionally(e -> {
                        log.error("handle waving order failed.", e);
                        return null;
                    });
        });

    }

    public void wavePickingSingleWarehouse(List<Long> orderIds, String key) {

        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(orderIds);

        List<OutboundPlanOrder> newOrders = outboundPlanOrders.stream()
                .filter(v -> v.getOutboundPlanOrderStatus() == OutboundPlanOrderStatusEnum.ASSIGNED).toList();
        if (CollectionUtils.isEmpty(newOrders)) {
            log.error("lists can't be empty, there maybe something error. remove it: {} from redis.", orderIds);
            redisUtils.removeListByPureKey(key, orderIds);
            return;
        }

        Collection<List<OutboundPlanOrder>> lists = outboundWaveService.wavePickings(newOrders);
        if (CollectionUtils.isEmpty(lists)) {
            return;
        }

        lists.forEach(list -> {
            outboundWaveAggregate.waveOrders(list);
            redisUtils.removeListByPureKey(key, list.stream().map(OutboundPlanOrder::getId).toList());
        });
    }
}
