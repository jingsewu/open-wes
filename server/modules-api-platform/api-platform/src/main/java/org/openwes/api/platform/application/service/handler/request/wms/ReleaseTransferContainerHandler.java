package org.openwes.api.platform.application.service.handler.request.wms;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.wes.api.outbound.dto.TransferContainerReleaseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReleaseTransferContainerHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.TRANSFER_CONTAINER_RELEASE.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        List<TransferContainerReleaseDTO> releaseDTOS = getTargetList(context, TransferContainerReleaseDTO.class);
        coreClientService.transferContainerRelease(releaseDTOS);
    }
}
