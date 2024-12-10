package org.openwes.api.platform.infrastructure;

import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface EmsClientService {

    void containerArrive(ContainerArrivedEvent containerArrivedEvent);

    void updateContainerTaskStatus(@Valid List<UpdateContainerTaskDTO> containerTaskDTOS);
}
