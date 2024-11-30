package org.openwes.wes.outbound.application.scheduler;

import com.alibaba.ttl.TtlRunnable;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.event.NewOutboundWaveEvent;
import org.openwes.wes.outbound.domain.aggregate.OutboundWaveAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.service.OutboundWaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final DistributeLock distributeLock;

    @Autowired
    @Qualifier("wavePickingExecutor")
    private Executor wavePickingExecutor;

    @Scheduled(cron = "${wms.schedule.config.wavePicking:0 0/5 * * * *}")
    public void wavePicking() {
        List<String> keys = redisUtils.keys(RedisUtils.generateKeysPatten("", OUTBOUND_PLAN_ORDER_ASSIGNED_IDS));
        keys.forEach(key -> {
            List<Long> orderIds = redisUtils.getListByPureKey(key);
            if (CollectionUtils.isEmpty(orderIds)) {
                return;
            }

            CompletableFuture
                    .runAsync(Objects.requireNonNull(TtlRunnable.get(()
                            -> this.tryWavePickingSingleWarehouse(orderIds, key))), wavePickingExecutor)
                    .exceptionally(e -> {
                        log.error("handle waving order failed.", e);
                        return null;
                    });
        });

    }

    private void tryWavePickingSingleWarehouse(List<Long> orderIds, String key) {
        boolean acquireLock = distributeLock.acquireLock(RedisConstants.OUTBOUND_WAVE_SCHEDULE_EXECUTE_LOCK, 0);
        if (acquireLock) {
            try {
                wavePickingSingleWarehouse(orderIds, key);
            } finally {
                distributeLock.releaseLock(RedisConstants.OUTBOUND_WAVE_SCHEDULE_EXECUTE_LOCK);
            }
        }
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
            String waveNo = outboundWaveAggregate.waveOrders(list);
            DomainEventPublisher.sendAsyncDomainEvent(new NewOutboundWaveEvent().setWaveNo(waveNo));
            redisUtils.removeListByPureKey(key, list.stream().map(OutboundPlanOrder::getId).toList());
        });
    }
}
