package org.openwes.wes.api.basic;

import jakarta.validation.Valid;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.task.dto.TransferContainerReleaseDTO;

import java.util.List;

public interface ITransferContainerApi {

    void containerArrive(@Valid ContainerArrivedEvent containerArrivedEvent);

    void transferContainerRelease(@Valid List<TransferContainerReleaseDTO> releaseDTOS);

    void release(Long id);
}
