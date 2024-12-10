package org.openwes.api.platform.application.service.handler;

import com.alibaba.fastjson2.JSONObject;
import org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum;
import org.openwes.api.platform.application.context.CallbackHandleContext;
import org.openwes.api.platform.application.service.CallbackHandlerService;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.api.platform.infrastructure.WmsClientService;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.common.utils.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class CallbackHandler implements CallbackHandlerService {

    @Autowired
    protected WmsClientService coreClientService;

    @Override
    public void convert(CallbackHandleContext context) {
        Object targetObj = ConverterHelper.convertParam(context.getApiConfig(), context.getSourceData());
        if (targetObj == null) {
            context.setIgnoreCallback(true);
        }
        context.setTargetData(targetObj);
    }

    @Override
    public void beforeConvert(CallbackHandleContext context) {
    }

    @Override
    public void afterConvert(CallbackHandleContext context) {
    }

    @Override
    public void invoke(CallbackHandleContext context) {
        //判断是否无需回传：1、接口配置回传忽略，2、afterConvert场景中的插件可能处理为无需发送
        if (context.isIgnoreCallback() || context.getTargetData() == null) {
            return;
        }
        Object result = invoke(context.getApiPO(), context.getTargetData());

        //异常
        if (result instanceof Response response) {
            context.setResponse(response);
            return;
        }

        //通过模板解析回调结果
        Response response;
        Object convertResponse = ConverterHelper.convertResponse(context.getApiConfig(), result);
        if (convertResponse == null) {
            response = Response.builder().code(ApiPlatformErrorCodeEnum.API_TEMPLATE_PARSE_ERROR.getCode()).build();
        } else {
            response = JSONObject.parseObject(JsonUtils.obj2String(convertResponse), Response.class);
        }
        context.setResponse(response);
    }

    protected Object invoke(ApiPO apiPO, Object targetObj) {
        Object response;
        try {
            response = apiPO.execute(targetObj);
        } catch (Exception e) {
            log.error("execute callback error:", e);
            response = Response.builder().code(CommonErrorDescEnum.SYSTEM_EXEC_ERROR.getCode()).msg(e.getMessage()).build();
        }
        log.debug("external request:\napiPo is {}\ntarget obj is {}\nresponse obj is {}", apiPO, targetObj, response);
        return response;
    }

    @Override
    public void afterInvoke(CallbackHandleContext context) {
    }

}
