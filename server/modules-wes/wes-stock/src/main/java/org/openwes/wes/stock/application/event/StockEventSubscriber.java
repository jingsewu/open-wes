package org.openwes.wes.stock.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.basic.event.ContainerStockUpdateEvent;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.api.stock.event.StockClearEvent;
import org.openwes.wes.api.stock.event.StockCreateEvent;
import org.openwes.wes.api.stock.event.StockTransferEvent;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.stock.domain.aggregate.SkuBatchContainerStockAggregate;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@Validated
@Slf4j
@RequiredArgsConstructor
public class StockEventSubscriber {

    private final SkuBatchContainerStockAggregate skuBatchContainerStockAggregate;
    private final ContainerStockRepository containerStockRepository;

    /**
     * event is available for stock transfer from one container to another.
     * applying scenes like: put away, piking, adjust and so on.
     *
     * @param event
     */
    @Subscribe
    public void onStockTransferEvent(@Valid StockTransferEvent event) {

        log.info("stock module receive stock transfer event: {}", event.toString());

        StockTransferDTO stockTransferDTO = event.getStockTransferDTO();
        ContainerStock containerStock = containerStockRepository.findById(stockTransferDTO.getContainerStockId());
        if (event.getTaskType() == OperationTaskTypeEnum.PICKING || event.getTaskType() == OperationTaskTypeEnum.ADJUST) {
            skuBatchContainerStockAggregate.transferAndUnlockStock(stockTransferDTO, containerStock, event.getEventId());
        } else {
            skuBatchContainerStockAggregate.transferStock(stockTransferDTO, containerStock, event.getEventId());
        }

        DomainEventPublisher.sendAsyncDomainEvent(new ContainerStockUpdateEvent()
                .setContainerCode(containerStock.getContainerCode())
                .setWarehouseCode(containerStock.getWarehouseCode())
        );
    }

    /**
     * event is only available for the first time stock put into the container
     * and the container does not exist the current sku before.
     * Applying Scenes like: receiving
     *
     * @param event
     */
    @Subscribe
    public void onStockCreateEvent(@Valid StockCreateEvent event) {

        log.info("stock module receive stock create event: {}", event.toString());

        skuBatchContainerStockAggregate.createStock(event.getStockCreateDTO(), event.getEventId());

        DomainEventPublisher.sendAsyncDomainEvent(new ContainerStockUpdateEvent()
                .setContainerCode(event.getStockCreateDTO().getTargetContainerCode())
                .setWarehouseCode(event.getStockCreateDTO().getWarehouseCode())
        );
    }

    @Subscribe
    public void onStockClearEvent(@Valid StockClearEvent event) {
        log.info("stock module receive stock clear event: {}", event.toString());
        List<ContainerStock> containerStocks = containerStockRepository.findAllByIds(event.getContainerStockIds());
        if (CollectionUtils.isEmpty(containerStocks)) {
            log.warn("cannot find container stocks, ids: {}", event.getContainerStockIds());
            return;
        }
        skuBatchContainerStockAggregate.clearStock(containerStocks);
    }

}
