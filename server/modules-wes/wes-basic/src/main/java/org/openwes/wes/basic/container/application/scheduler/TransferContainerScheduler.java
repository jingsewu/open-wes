package org.openwes.wes.basic.container.application.scheduler;

import com.alibaba.ttl.TtlRunnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.constants.TransferContainerReleaseMethodEnum;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.basic.container.domain.entity.TransferContainer;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransferContainerScheduler {

    private final Executor unlockTransferContainerExecutor;
    private final TransferContainerRepository transferContainerRepository;
    private final DistributeLock distributeLock;
    private final ISystemConfigApi systemConfigApi;

    private static final int LIMIT_SIZE_PER_TIME = 100;

    @Scheduled(cron = "0 0/10 * * * *")
    public void handleTransferContainer() {

        CompletableFuture
                .runAsync(TtlRunnable.get(this::tryUnlockTransferContainer), unlockTransferContainerExecutor)
                .exceptionally(e -> {
                    log.error("unlock transfer container failed.", e);
                    return null;
                });
    }

    private void tryUnlockTransferContainer() {

        boolean acquireLock = distributeLock.acquireLock(RedisConstants.TRANSFER_CONTAINER_UNLOCK_SCHEDULE_EXECUTE_LOCK, 0);
        if (acquireLock) {
            try {
                unlockTransferContainer();
            } finally {
                distributeLock.releaseLock(RedisConstants.TRANSFER_CONTAINER_UNLOCK_SCHEDULE_EXECUTE_LOCK);
            }
        }
    }

    private void unlockTransferContainer() {

        SystemConfigDTO.BasicConfigDTO basicConfig = systemConfigApi.getBasicConfig();
        if (basicConfig.getTransferContainerReleaseMethod() != TransferContainerReleaseMethodEnum.AUTOMATED) {
            return;
        }

        List<TransferContainer> transferContainers = transferContainerRepository.findAllLockedContainers(1)
                .stream()
                .filter(v -> v.getLockedTime() + (long) basicConfig.getAutoReleaseDelayTimeMin() * 60 * 1000 < System.currentTimeMillis())
                .limit(LIMIT_SIZE_PER_TIME)
                .toList();

        if (CollectionUtils.isEmpty(transferContainers)) {
            return;
        }

        transferContainers.forEach(TransferContainer::unlock);
        transferContainerRepository.saveAll(transferContainers);
    }
}
