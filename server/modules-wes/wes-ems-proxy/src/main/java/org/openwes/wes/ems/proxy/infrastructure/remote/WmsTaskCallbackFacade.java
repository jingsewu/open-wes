package org.openwes.wes.ems.proxy.infrastructure.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.task.event.TransferContainerArrivedEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WmsTaskCallbackFacade {

    public void transferContainerArrive(ContainerArrivedEvent containerArrivedEvent) {

        List<TransferContainerArrivedEvent.TransferContainerArriveDetail> details = containerArrivedEvent.getContainerDetails().stream().map(v ->
                new TransferContainerArrivedEvent.TransferContainerArriveDetail()
                        .setContainerCode(v.getContainerCode())
                        .setLocationCode(v.getLocationCode())).toList();

        DomainEventPublisher.sendAsyncDomainEvent(new TransferContainerArrivedEvent()
                .setWarehouseAreaId(containerArrivedEvent.getWarehouseAreaId()).setDetails(details));
    }
}
