package org.openwes.api.platform.infrastructure;

import org.openwes.wes.api.ems.proxy.IContainerOperatorApi;
import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class EmsClientServiceImpl implements EmsClientService {

    @DubboReference
    private IContainerTaskApi containerTaskApi;
    @DubboReference
    private IContainerOperatorApi containerArriveApi;

    @Override
    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        containerArriveApi.containerArrive(containerArrivedEvent);
    }

    @Override
    public void updateContainerTaskStatus(List<UpdateContainerTaskDTO> containerTaskDTOS) {
        containerTaskApi.updateContainerTaskStatus(containerTaskDTOS);
    }
}
