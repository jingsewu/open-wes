package org.openwes.api.platform.application.service.handler.request.ems;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.infrastructure.EmsClientService;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.ApiPlatformErrorDescEnum;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ContainerTaskStatusUpdateHandler extends RequestHandler {

    private final EmsClientService emsClientService;

    @Override
    public void invoke(RequestHandleContext context) {
        List<UpdateContainerTaskDTO> containerTaskDTOS = getTargetList(context, UpdateContainerTaskDTO.class)
                .stream().filter(Objects::nonNull).toList();

        if (CollectionUtils.isEmpty(containerTaskDTOS)) {
            throw WmsException.throwWmsException(ApiPlatformErrorDescEnum.EMS_CONTAINER_TASKS_IS_EMPTY);
        }

        emsClientService.updateContainerTaskStatus(containerTaskDTOS);
    }

    @Override
    public String getApiType() {
        return ApiTypeEnum.CONTAINER_TASK_STATUS_REPORT.name();
    }

}
