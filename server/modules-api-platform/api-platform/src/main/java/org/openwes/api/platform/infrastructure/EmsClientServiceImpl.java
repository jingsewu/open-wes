package org.openwes.api.platform.infrastructure;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.ems.proxy.IContainerOperatorApi;
import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class EmsClientServiceImpl implements EmsClientService {

    private final IContainerTaskApi containerTaskApi;
    private final IContainerOperatorApi containerArriveApi;

    @Override
    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        containerArriveApi.containerArrive(containerArrivedEvent);
    }

    @Override
    public void updateContainerTaskStatus(List<UpdateContainerTaskDTO> containerTaskDTOS) {
        containerTaskApi.updateContainerTaskStatus(containerTaskDTOS);
    }
}
