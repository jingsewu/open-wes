package org.openwes.api.platform.application.service.handler.request;

import lombok.RequiredArgsConstructor;
import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.service.handler.AbstractRequestHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomRequestHandler extends AbstractRequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.CUSTOM_API.name();
    }

}
