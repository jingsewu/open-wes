package org.openwes.api.platform.application.service.handler.request.ems;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.infrastructure.EmsClientService;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContainerArriveRequestHandler extends RequestHandler {

    private final EmsClientService emsClientService;

    @Override
    public void invoke(RequestHandleContext context) {
        List<ContainerArrivedEvent> list = getTargetList(context, ContainerArrivedEvent.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(emsClientService::containerArrive);
    }

    @Override
    public String getApiType() {
        return ApiTypeEnum.CONTAINER_ARRIVE.name();
    }

}
