package org.openwes.api.platform.application.service.handler;

import com.google.common.collect.Maps;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.application.service.handler.callback.CommonCallbackHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CallbackHandlerFactory implements InitializingBean {

    private static final Map<CallbackApiTypeEnum, CallbackHandler> map = Maps.newHashMap();

    private final List<CallbackHandler> handlerList;
    private final CommonCallbackHandler commonCallbackHandler;

    @Autowired
    public CallbackHandlerFactory(List<CallbackHandler> handlerList, CommonCallbackHandler commonCallbackHandler) {
        this.handlerList = handlerList;
        this.commonCallbackHandler = commonCallbackHandler;
    }

    @Override
    public void afterPropertiesSet() {
        for (CallbackHandler handler : handlerList) {
            map.put(handler.getCallbackType(), handler);
        }
    }

    public CallbackHandler getHandler(CallbackApiTypeEnum callbackType) {
        return map.getOrDefault(callbackType, commonCallbackHandler);
    }
}
