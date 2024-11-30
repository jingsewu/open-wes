package org.openwes.api.platform.application.service.impl;

import com.alibaba.fastjson2.JSON;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum;
import org.openwes.api.platform.application.context.CallbackHandleContext;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.HandlerExecutor;
import org.openwes.api.platform.application.service.handler.CallbackHandler;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.api.platform.application.service.handler.RequestHandlerFactory;
import org.openwes.api.platform.aspect.ApiLog;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.service.ApiConfigService;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.api.platform.utils.AssertUtils;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.api.platform.utils.ResponseUtils;
import org.openwes.common.utils.http.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum.API_API_IS_NOT_ENABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandlerExecutorImpl implements HandlerExecutor {

    private final RequestHandlerFactory requestHandlerFactory;
    private final ApiConfigService apiConfigService;
    private final ApiService apiService;

    @Override
    @ApiLog(apiCode = "#apiType")
    public Object executeRequest(String apiType, String body) {
        RequestHandler handler = requestHandlerFactory.getHandler(apiType);

        RequestHandleContext handleContext = new RequestHandleContext();
        try {
            AssertUtils.notNull(handler, ApiPlatformErrorCodeEnum.API_API_TYPE_ERROR, new Object[]{apiType});

            ApiPO apiPO = apiService.getByCode(apiType);
            AssertUtils.notNull(apiPO, ApiPlatformErrorCodeEnum.API_API_TYPE_NOT_EXIST, new Object[]{apiType});
            AssertUtils.checkExpression(apiPO.isEnabled(), API_API_IS_NOT_ENABLE, new Object[]{apiType});

            ApiConfigPO apiConfigPO = apiConfigService.getByCode(apiType);
            handleContext.setApiType(handler.getApiType());
            handleContext.setApiConfig(apiConfigPO);

            handleContext.setBody(body);

            handler.convertParam(handleContext);

            handler.validate(handleContext);

            handler.supply(handleContext);

            handler.saveData(handleContext);

            handler.invoke(handleContext);

            handler.afterInvoke(handleContext);

            return handler.response(handleContext);
        } catch (Exception e) {
            log.error("handle customer request error.apiType:{},body:{}", apiType, body, e);
            return ConverterHelper.convertResponse(handleContext.getApiConfig(), ResponseUtils.buildResponse(e));
        }
    }

    /**
     * 执行回调处理
     */
    @Override
    @ApiLog(apiCode = "#apiPO.code", messageId = "#sourceData.messageId")
    @Async(value = "callbackExecutor")
    public <T> CompletableFuture<Response> executeCallback(CallbackHandler handler, ApiPO apiPO, CallbackMessage<T> sourceData) {
        return CompletableFuture.completedFuture(executeCallbackWithoutLog(handler, apiPO, sourceData));
    }

    @ApiLog(apiCode = "#apiPO.code", messageId = "#sourceData != null ? #sourceData.messageId : ''")
    @Override
    public <T> Response synchronizeExecuteCallback(CallbackHandler handler, ApiPO apiPO, CallbackMessage<T> sourceData) {
        return executeCallbackWithoutLog(handler, apiPO, sourceData);
    }

    @Override
    public Response executeCallbackWithoutLog(CallbackHandler handler, ApiPO apiPO, Object sourceData) {

        try {

            CallbackHandleContext handleContext = new CallbackHandleContext();
            handleContext.setSourceData(sourceData);

            handleContext.setApiPO(apiPO);
            ApiConfigPO apiConfigPO = apiConfigService.getByCode(apiPO.getCode());
            handleContext.setApiConfig(apiConfigPO);

            handler.beforeConvert(handleContext);

            handler.convert(handleContext);

            handler.afterConvert(handleContext);

            handler.invoke(handleContext);

            handler.afterInvoke(handleContext);

            return handleContext.getResponse();
        } catch (Exception e) {
            log.error("handler callback error.callbackType: {},sourceData: {}", apiPO.getCode(),
                    JSON.toJSONString(sourceData), e);
            return ResponseUtils.buildResponse(e);
        }
    }
}
