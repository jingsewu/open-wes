package org.openwes.wes.api.task;

import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.outbound.dto.TransferContainerReleaseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ITransferContainerApi {

    void containerArrive(@Valid ContainerArrivedEvent containerArrivedEvent);

    void transferContainerRelease(@Valid List<TransferContainerReleaseDTO> releaseDTOS);
}
