package org.openwes.api.platform.application.service.handler.callback;

import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.application.service.handler.AbstractCallbackHandler;
import org.springframework.stereotype.Component;

@Component
public class ContainerTaskCreateCallback extends AbstractCallbackHandler {

    @Override
    public CallbackApiTypeEnum getCallbackType() {
        return CallbackApiTypeEnum.CONTAINER_TASK_CREATE;
    }

}
