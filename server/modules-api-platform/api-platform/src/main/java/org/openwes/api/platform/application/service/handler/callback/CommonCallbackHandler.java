package org.openwes.api.platform.application.service.handler.callback;

import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.application.service.handler.AbstractCallbackHandler;
import org.springframework.stereotype.Service;

@Service
public class CommonCallbackHandler extends AbstractCallbackHandler {

    @Override
    public CallbackApiTypeEnum getCallbackType() {
        return CallbackApiTypeEnum.COMMON_CALLBACK;
    }

}
