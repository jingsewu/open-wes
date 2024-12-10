package org.openwes.api.platform.application.service.handler.request.wms;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.wes.api.basic.dto.ContainerLocationReportDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContainerLocationReportHandler extends RequestHandler {

    @Override
    public String getApiType() {
        return ApiTypeEnum.CONTAINER_LOCATION_REPORT.name();
    }

    @Override
    public void invoke(RequestHandleContext context) {
        List<ContainerLocationReportDTO> reportDTOS = getTargetList(context, ContainerLocationReportDTO.class);

        if (CollectionUtils.isNotEmpty(reportDTOS)) {
            coreClientService.containerLocationReport(reportDTOS);
        }
    }
}
