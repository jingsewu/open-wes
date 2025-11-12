package org.openwes.wes.basic.container.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.basic.event.ContainerLocationUpdateEvent;
import org.openwes.wes.api.basic.event.ContainerStockUpdateEvent;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.basic.container.domain.entity.Container;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerEventSubscriber {

    private final ContainerRepository containerRepository;
    private final IStockApi stockApi;
    private final DistributeLock distributeLock;

    @Subscribe
    public void onContainerStockUpdate(@Valid ContainerStockUpdateEvent event) {
        log.info("receive container stock update event: {}", event);

        //default lock 3 seconds when received the same container update event to avoid duplicate update
        boolean acquireLock = distributeLock.acquireLock(RedisConstants.CONTAINER_STOCK_UPDATE_LOCK
                + event.getContainerCode() + ":" + event.getWarehouseCode(), 0, 5000L);
        if (!acquireLock) {
            return;
        }
        List<ContainerStockDTO> containerStocks = stockApi.getContainerStocks(
                Lists.newArrayList(event.getContainerCode()), event.getWarehouseCode());

        Container container = containerRepository.findByContainerCode(event.getContainerCode(), event.getWarehouseCode());
        if (container == null) {
            log.warn("no container found with containerCode: {}, warehouseCode: {}", event.getContainerCode(), event.getWarehouseCode());
            return;
        }
        container.changeStocks(containerStocks);

        containerRepository.save(container);
    }

    @Subscribe
    public void onContainerLocationUpdate(@Valid ContainerLocationUpdateEvent event) {
        log.info("receive container location update event: {}", event);

        //default lock 3 seconds when received the same container update event to avoid duplicate update
        boolean acquireLock = distributeLock.acquireLock(RedisConstants.CONTAINER_LOCATION_UPDATE_LOCK
                + event.getContainerCode() + ":" + event.getWarehouseCode(), 0, 5000L);
        if (!acquireLock) {
            return;
        }

        Container container = containerRepository.findByContainerCode(event.getContainerCode(), event.getWarehouseCode());
        if (container == null) {
            log.warn("no container found with containerCode: {}, warehouseCode: {} when update container location",
                    event.getContainerCode(), event.getWarehouseCode());
            return;
        }

        container.changeLocation(event.getWarehouseCode(), event.getWarehouseAreaId(), event.getLocationCode(), "");
        container.moveInside(event.getWarehouseCode(), event.getWarehouseAreaId(), event.getLocationCode());
        containerRepository.save(container);

    }
}
