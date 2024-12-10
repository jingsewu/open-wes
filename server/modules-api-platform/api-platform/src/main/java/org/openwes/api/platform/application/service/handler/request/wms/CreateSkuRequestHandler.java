package org.openwes.api.platform.application.service.handler.request.wms;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateSkuRequestHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.SKU_CREATE.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        List<SkuMainDataDTO> list = getTargetList(context, SkuMainDataDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int count = 0;
        if (ConverterHelper.isAsyncApi(context.getApiType(), list.size())) {
            count = list.size();
            coreClientService.asyncCreateOrUpdateSku(list);
        } else {
            count = coreClientService.createOrUpdateSku(list);
        }
        context.setResponse(Response.builder().data(count).build());
    }
}
