package org.openwes.api.platform.application;

import com.alibaba.ttl.TtlRunnable;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.api.platform.application.service.HandlerExecutor;
import org.openwes.api.platform.application.service.handler.CallbackHandler;
import org.openwes.api.platform.application.service.handler.CallbackHandlerFactory;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.common.utils.http.Response;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
@DubboService
public class CallbackApiImpl implements ICallbackApi {

    private final HandlerExecutor handlerExecutor;
    private final CallbackHandlerFactory callbackHandlerFactory;
    private final ApiService apiService;
    private final Executor callbackExecutor;

    @Override
    public <T> Response callback(CallbackApiTypeEnum callbackType, String bizType, CallbackMessage<T> sourceData) {

        CallbackHandler handler = callbackHandlerFactory.getHandler(callbackType);

        ApiPO apiPO = apiService.getByCode(callbackType.name());
        if (apiPO == null && StringUtils.isNotEmpty(bizType)) {
            //由于回传可能根据客户的订单状态来回传到不通的系统，所以增加bizType-customerOrderType的判断
            apiPO = apiService.getByCode(ApiPO.generateCode(callbackType, bizType));
        }
        if (apiPO == null) {
            log.error("api config is not exist，callbackType: {},bizType: {}", callbackType, bizType);
            return null;
        }
        if (!apiPO.isEnabled()) {
            log.warn("api is not enable，callbackType: {},bizType: {}", callbackType, bizType);
            return null;
        }

        if (apiPO.isSyncCallback()) {
            return handlerExecutor.synchronizeExecuteCallback(handler, apiPO, sourceData);
        }

        ApiPO finalApiPO = apiPO;
        CompletableFuture.runAsync(Objects.requireNonNull(
                                TtlRunnable.get(() -> handlerExecutor.synchronizeExecuteCallback(handler, finalApiPO, sourceData)))
                        , callbackExecutor)
                .exceptionally(e -> {
                    log.error("callback error", e);
                    return null;
                });
        return null;
    }
}
