package org.openwes.api.platform.application.service.handler.request.wms;


import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderCancelDTO;
import org.springframework.stereotype.Component;

@Component
public class CancelInboundRequestHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.ORDER_INBOUND_CANCEL.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        final InboundPlanOrderCancelDTO inboundPlanOrderCancelDTO = JsonUtils.string2Object(context.getBody(), InboundPlanOrderCancelDTO.class);
        coreClientService.cancelInboundOrder(inboundPlanOrderCancelDTO);
        context.setResponse(Response.builder().build());
    }
}
