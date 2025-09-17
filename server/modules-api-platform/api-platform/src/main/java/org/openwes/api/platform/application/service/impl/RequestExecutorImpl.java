package org.openwes.api.platform.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum;
import org.openwes.api.platform.application.service.RequestExecutor;
import org.openwes.api.platform.application.service.handler.AbstractRequestHandler;
import org.openwes.api.platform.application.service.handler.RequestHandlerFactory;
import org.openwes.api.platform.aspect.ApiLog;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.domain.service.ApiConfigService;
import org.openwes.api.platform.domain.service.ApiService;
import org.openwes.api.platform.domain.transfer.ApiConfigTransfer;
import org.openwes.api.platform.utils.AssertUtils;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.api.platform.utils.ResponseUtils;
import org.springframework.stereotype.Service;

import static org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum.API_API_IS_NOT_ENABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestExecutorImpl implements RequestExecutor {

    private final RequestHandlerFactory requestHandlerFactory;
    private final ApiConfigService apiConfigService;
    private final ApiService apiService;
    private final ApiConfigTransfer apiConfigTransfer;

    @Override
    @ApiLog(apiCode = "#apiType")
    public Object executeRequest(String apiType, String body) {
        AbstractRequestHandler handler = requestHandlerFactory.getHandler(apiType);

        RequestHandleContext handleContext = new RequestHandleContext();
        try {
            AssertUtils.notNull(handler, ApiPlatformErrorCodeEnum.API_API_TYPE_ERROR, new Object[]{apiType});

            ApiPO apiPO = apiService.getByCode(apiType);
            AssertUtils.notNull(apiPO, ApiPlatformErrorCodeEnum.API_API_TYPE_NOT_EXIST, new Object[]{apiType});
            AssertUtils.checkExpression(apiPO.isEnabled(), API_API_IS_NOT_ENABLE, new Object[]{apiType});

            ApiConfigPO apiConfigPO = apiConfigService.getByCode(apiType);
            handleContext.setApiType(handler.getApiType());
            handleContext.setApiConfig(apiConfigTransfer.toDTO(apiConfigPO));

            handleContext.setBody(body);

            handler.convertParam(handleContext);

            handler.validate(handleContext);

            handler.supply(handleContext);

            handler.invoke(handleContext);

            handler.afterInvoke(handleContext);

            return handler.response(handleContext);
        } catch (Exception e) {
            log.error("handle customer request error.apiType:{},body:{}", apiType, body, e);
            return ConverterHelper.convertResponse(apiConfigTransfer.toDO(handleContext.getApiConfig())
                    , ResponseUtils.buildResponse(e));
        }
    }

}
