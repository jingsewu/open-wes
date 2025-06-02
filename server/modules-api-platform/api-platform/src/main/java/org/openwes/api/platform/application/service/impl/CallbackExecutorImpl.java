package org.openwes.api.platform.application.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.dto.callback.CallbackHandleContext;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum;
import org.openwes.api.platform.application.service.CallbackExecutor;
import org.openwes.api.platform.application.service.RequestExecutor;
import org.openwes.api.platform.application.service.handler.AbstractCallbackHandler;
import org.openwes.api.platform.application.service.handler.AbstractRequestHandler;
import org.openwes.api.platform.application.service.handler.RequestHandlerFactory;
import org.openwes.api.platform.aspect.ApiLog;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.service.ApiConfigService;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.api.platform.domain.transfer.ApiConfigTransfer;
import org.openwes.api.platform.domain.transfer.ApiTransfer;
import org.openwes.api.platform.utils.AssertUtils;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.api.platform.utils.ResponseUtils;
import org.openwes.common.utils.http.Response;
import org.springframework.stereotype.Service;

import static org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum.API_API_IS_NOT_ENABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallbackExecutorImpl implements CallbackExecutor {

    private final ApiConfigService apiConfigService;
    private final ApiTransfer apiTransfer;
    private final ApiConfigTransfer apiConfigTransfer;

    @ApiLog(apiCode = "#apiPO.code", messageId = "#sourceData != null ? #sourceData.messageId : ''")
    @Override
    public <T> Response synchronizeExecuteCallback(AbstractCallbackHandler handler, ApiPO apiPO, CallbackMessage<T> sourceData) {
        return executeCallbackWithoutLog(handler, apiPO, sourceData);
    }

    @Override
    public Response executeCallbackWithoutLog(AbstractCallbackHandler handler, ApiPO apiPO, Object sourceData) {

        try {

            CallbackHandleContext handleContext = new CallbackHandleContext();
            handleContext.setSourceData(sourceData);

            handleContext.setApi(apiTransfer.toDTO(apiPO));
            ApiConfigPO apiConfigPO = apiConfigService.getByCode(apiPO.getCode());
            handleContext.setApiConfig(apiConfigTransfer.toDTO(apiConfigPO));

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
