package org.openwes.api.platform.application.service;

import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.application.context.CallbackHandleContext;

public interface CallbackHandlerService {

    CallbackApiTypeEnum getCallbackType();

    void beforeConvert(CallbackHandleContext context);

    void convert(CallbackHandleContext context);

    void afterConvert(CallbackHandleContext context);

    void invoke(CallbackHandleContext context);

    void afterInvoke(CallbackHandleContext context);

}
