package org.openwes.wes.basic.container.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.openwes.wes.api.basic.event.ContainerStockUpdateEvent;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.basic.container.domain.entity.Container;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerEventSubscriber {

    private final ContainerRepository containerRepository;
    private final IStockApi stockApi;

    @Subscribe
    public void onContainerStockUpdate(@Valid ContainerStockUpdateEvent event) {
        log.info("Receive container stock update event: {}", event);

        Container container = containerRepository.findByContainerCode(event.getContainerCode(), event.getWarehouseCode());
        if (container == null) {
            return;
        }

        List<ContainerStockDTO> containerStocks = stockApi.getContainerStocks(Lists.newArrayList(event.getContainerCode()), event.getWarehouseCode());
        container.changeStocks(containerStocks);
        containerRepository.save(container);
    }
}
