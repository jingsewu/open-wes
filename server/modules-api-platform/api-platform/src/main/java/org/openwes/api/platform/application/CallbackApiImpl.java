package org.openwes.api.platform.application;

import com.alibaba.ttl.TtlRunnable;
import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.api.platform.application.service.HandlerExecutor;
import org.openwes.api.platform.application.service.handler.CallbackHandler;
import org.openwes.api.platform.application.service.handler.CallbackHandlerFactory;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.common.utils.http.Response;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
@DubboService
public class CallbackApiImpl implements ICallbackApi {

    private final HandlerExecutor handlerExecutor;
    private final CallbackHandlerFactory callbackHandlerFactory;
    private final ApiService apiService;

    @Override
    public <T> Response callback(CallbackApiTypeEnum callbackType, String bizType, CallbackMessage<T> sourceData) {

        //1.1:业务场景（服务）路由
        CallbackHandler handler = callbackHandlerFactory.getHandler(callbackType);

        //1.1 查询接口配置，由于回传可能根据客户的订单状态来回传到不通的系统，所以增加bizType-customerOrderType的判断
        ApiPO apiPO = apiService.getByCode(callbackType.name());
        if (apiPO == null && StringUtils.isNotEmpty(bizType)) {
            apiPO = apiService.getByCode(ApiPO.generateCode(callbackType, bizType));
        }
        if (apiPO == null) {
            log.warn("api config is not exist，callbackType: {},bizType: {}", callbackType, bizType);
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
                        TtlRunnable.get(() -> handlerExecutor.synchronizeExecuteCallback(handler, finalApiPO, sourceData))))
                .exceptionally(e -> {
                    log.error("callback error", e);
                    return null;
                });
        return null;
    }
}
