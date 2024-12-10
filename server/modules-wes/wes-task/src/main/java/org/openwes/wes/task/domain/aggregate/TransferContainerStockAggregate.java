package org.openwes.wes.task.domain.aggregate;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.event.StockClearEvent;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferContainerStockAggregate {

    private final IStockApi stockApi;
    private final TransferContainerRepository transferContainerRepository;

    @Transactional(rollbackFor = Exception.class)
    public void releaseTransferContainers(List<TransferContainer> transferContainers) {
        transferContainers.forEach(TransferContainer::unlock);
        transferContainerRepository.saveAll(transferContainers);

        clearTransferContainerStock(transferContainers);
    }

    private void clearTransferContainerStock(List<TransferContainer> transferContainers) {
        Map<String, Set<String>> containerCodeGroupMap = transferContainers.stream()
                .collect(Collectors.groupingBy(TransferContainer::getWarehouseCode, Collectors.mapping(TransferContainer::getTransferContainerCode, Collectors.toSet())));


        List<ContainerStockDTO> containerStockDTOS = containerCodeGroupMap.entrySet().stream()
                .flatMap(entry -> stockApi.getContainerStocks(entry.getValue(), entry.getKey()).stream()).toList();

        containerStockDTOS.stream()
                .filter(v -> !Objects.equals(v.getAvailableQty(), v.getTotalQty()))
                .findAny().ifPresent(v -> {
                    throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_HAS_UNAVAILABLE_STOCK, v.getContainerCode());
                });

        List<Long> containerStockIds = containerStockDTOS.stream().map(ContainerStockDTO::getId).toList();

        DomainEventPublisher.sendAsyncDomainEvent(new StockClearEvent(containerStockIds));
    }
}
