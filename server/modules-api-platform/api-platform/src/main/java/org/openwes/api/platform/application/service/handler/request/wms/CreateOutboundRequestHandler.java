package org.openwes.api.platform.application.service.handler.request.wms;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateOutboundRequestHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.ORDER_OUTBOUND_CREATE.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        List<OutboundPlanOrderDTO> list = getTargetList(context, OutboundPlanOrderDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (ConverterHelper.isAsyncApi(context.getApiType(), list.size())) {
            coreClientService.asyncCreateOutboundOrder(list);
        } else {
            coreClientService.createOutboundOrder(list);
        }
        context.setResponse(Response.builder().build());
    }
}
