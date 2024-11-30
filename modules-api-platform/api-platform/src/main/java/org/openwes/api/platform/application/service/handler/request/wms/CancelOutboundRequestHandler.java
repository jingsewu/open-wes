package org.openwes.api.platform.application.service.handler.request.wms;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CancelOutboundRequestHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.ORDER_OUTBOUND_CANCEL.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        final OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO = JsonUtils.string2Object(context.getBody(), OutboundPlanOrderCancelDTO.class);
        List<String> customerOrderNos = coreClientService.cancelOutboundOrder(outboundPlanOrderCancelDTO);
        assert outboundPlanOrderCancelDTO != null;
        outboundPlanOrderCancelDTO.setCustomerOrderNos(customerOrderNos);
        context.setResponse(Response.builder().data(outboundPlanOrderCancelDTO).build());
    }
}
