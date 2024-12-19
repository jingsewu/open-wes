package org.openwes.wes.basic.container.application.event;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.task.event.TransferContainerArrivedEvent;
import org.openwes.wes.basic.container.domain.entity.TransferContainer;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferContainerSubscriber {

    private final TransferContainerRepository transferContainerRepository;
    private final IWarehouseAreaApi warehouseAreaApi;

    @Subscribe
    public void onTransferContainerArrived(TransferContainerArrivedEvent transferContainerArriveEvent) {

        WarehouseAreaDTO warehouseAreaDTO = warehouseAreaApi.getById(transferContainerArriveEvent.getWarehouseAreaId());

        List<TransferContainer> transferContainers = transferContainerArriveEvent.getDetails().stream().map(v -> {
            TransferContainer transferContainer = transferContainerRepository.findByContainerCodeAndWarehouseCode(v.getContainerCode(), warehouseAreaDTO.getWarehouseCode());
            if (transferContainer == null) {
                log.warn("transfer container: {} not found at location: {}", v.getContainerCode(),
                        v.getLocationCode());
                return null;
            }

            transferContainer.updateLocation(warehouseAreaDTO.getId(), v.getLocationCode());

            return transferContainer;
        }).filter(Objects::nonNull).toList();

        transferContainerRepository.saveAll(transferContainers);
    }

}
