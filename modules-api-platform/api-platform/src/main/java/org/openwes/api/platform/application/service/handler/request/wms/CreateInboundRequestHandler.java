package org.openwes.api.platform.application.service.handler.request.wms;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateInboundRequestHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.ORDER_INBOUND_CREATE.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        List<InboundPlanOrderDTO> list = getTargetList(context, InboundPlanOrderDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        if (ConverterHelper.isAsyncApi(context.getApiType(), list.size())) {
            coreClientService.asyncCreateInboundOrder(list);
        } else {
            coreClientService.createInboundOrder(list);
        }
        context.setResponse(Response.builder().build());
    }

}
