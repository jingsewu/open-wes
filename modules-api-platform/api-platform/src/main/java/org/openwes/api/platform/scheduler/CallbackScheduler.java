package org.openwes.api.platform.scheduler;

import com.alibaba.ttl.TtlRunnable;
import com.google.common.collect.Maps;
import org.openwes.api.platform.api.constants.ApiCallTypeEnum;
import org.openwes.api.platform.api.constants.ApiLogStatusEnum;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.application.service.HandlerExecutor;
import org.openwes.api.platform.application.service.handler.CallbackHandler;
import org.openwes.api.platform.application.service.handler.CallbackHandlerFactory;
import org.openwes.api.platform.domain.entity.ApiLogPO;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.repository.ApiLogPORepository;
import org.openwes.api.platform.domain.repository.ApiPORepository;
import org.openwes.api.platform.domain.service.ApiLogService;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.http.Response;
import org.openwes.distribute.lock.DistributeLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class CallbackScheduler {

    private final Executor callbackExecutor;

    private final ApiLogPORepository apiLogPORepository;
    private final ApiPORepository apiPORepository;
    private final HandlerExecutor handlerExecutor;
    private final CallbackHandlerFactory callbackHandlerFactory;
    private final DistributeLock distributeLock;
    private final ApiLogService apiLogService;

    @Scheduled(fixedDelayString = "${api.fail.callback.fixedDelay:20000}")
    public void callbackSchedule() {

        log.debug("callback start...");

        TtlRunnable ttlRunnable = TtlRunnable.get(this::doCallback);

        CompletableFuture.runAsync(ttlRunnable, callbackExecutor)
                .exceptionally(e -> {
                    log.error("Failed to execute callback due to: {}", e.getMessage(), e);
                    return null;
                })
                .thenRun(() -> log.debug("callback execution finished successfully"));

        log.debug("callback finish...");
    }

    private void doCallback() {
        boolean acquireLock = distributeLock.acquireLock(RedisConstants.CALLBACK_SCHEDULE_EXECUTE_LOCK, 0);
        if (!acquireLock) {
            return;
        }
        try {
            int retryCount = 10;
            Date date = DateUtils.addDays(new Date(), -2);
            List<ApiLogPO> apiLogPOS = apiLogPORepository.findAllByStatusAndRetryCountLessThanAndCreateTimeAfter(
                    ApiLogStatusEnum.FAIL, retryCount, date.getTime());

            Map<String, ApiPO> apiPOMap = Maps.newHashMap();
            apiLogPOS.forEach(apiLogPO -> {

                ApiPO apiPO;
                if (apiPOMap.containsKey(apiLogPO.getApiCode())) {
                    apiPO = apiPOMap.get(apiLogPO.getApiCode());
                } else {
                    apiPO = apiPORepository.findByCode(apiLogPO.getApiCode());
                    apiPOMap.put(apiLogPO.getApiCode(), apiPO);
                }
                if (apiPO == null || apiPO.getApiType() == ApiCallTypeEnum.NOTIFY) {
                    return;
                }

                long startTime = System.currentTimeMillis();
                CallbackHandler handler = callbackHandlerFactory.getHandler(CallbackApiTypeEnum.valueOf(apiPO.resolveCallbackType()));
                Response response = handlerExecutor.executeCallbackWithoutLog(handler, apiPO, apiLogPO.getRequestData());

                apiLogPO.setRetryCount(apiLogPO.getRetryCount() + 1);
                if (Response.SUCCESS_CODE.equals(response.getCode())) {
                    apiLogPO.setStatus(ApiLogStatusEnum.SUCCESS);
                }

                long endTime = System.currentTimeMillis();

                apiLogPO.setCostTime(endTime - startTime);
            });
            apiLogPORepository.saveAll(apiLogPOS);
        } finally {
            distributeLock.releaseLock(RedisConstants.CALLBACK_SCHEDULE_EXECUTE_LOCK);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanApiLog() {

        log.debug("clean api log start...");
        CompletableFuture.runAsync(Objects.requireNonNull(TtlRunnable.get(this::doCleanApiLog)), callbackExecutor)
                .exceptionally((e) -> {
                    log.error("clean api log error:", e);
                    return null;
                }).thenRun(() -> log.debug("clean api log finished successfully"));
        log.debug("clean api log finish...");
    }

    private void doCleanApiLog() {
        Date date = DateUtils.addDays(new Date(), -7);
        apiLogService.removeByDate(date);
    }
}
