package org.openwes.api.platform.application.service.handler.request.wms;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.AbstractRequestHandler;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateInboundRequestHandler extends AbstractRequestHandler {

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
