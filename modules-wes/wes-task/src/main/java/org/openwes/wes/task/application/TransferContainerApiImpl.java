package org.openwes.wes.task.application;

import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.outbound.dto.TransferContainerReleaseDTO;
import org.openwes.wes.api.task.ITransferContainerApi;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.openwes.wes.task.domain.aggregate.TransferContainerStockAggregate;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class TransferContainerApiImpl implements ITransferContainerApi {

    private final IWarehouseAreaApi warehouseAreaApi;
    private final TransferContainerRepository transferContainerRepository;
    private final TransferContainerStockAggregate transferContainerStockAggregate;

    @Override
    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        ContainerArrivedEvent.ContainerDetail containerDetail = containerArrivedEvent.getContainerDetails()
                .stream().findFirst().orElseThrow();

        WarehouseAreaDTO warehouseAreaDTO = warehouseAreaApi.getById(containerArrivedEvent.getWarehouseAreaId());
        TransferContainer transferContainer = transferContainerRepository.findByContainerCodeAndWarehouseCode(containerDetail.getContainerCode(), warehouseAreaDTO.getWarehouseCode());
        if (transferContainer == null) {
            log.warn("transfer container: {} not found at location: {}", containerDetail.getContainerCode(),
                    containerArrivedEvent.getWorkLocationCode());
            return;
        }

        transferContainer.updateLocation(containerArrivedEvent.getWarehouseAreaId(), containerDetail.getLocationCode());
        transferContainerRepository.save(transferContainer);
    }

    @Override
    public void transferContainerRelease(List<TransferContainerReleaseDTO> releaseDTOS) {
        List<TransferContainer> lockedTransferContainers = releaseDTOS.stream().collect(Collectors.groupingBy(TransferContainerReleaseDTO::getWarehouseCode,
                        Collectors.mapping(TransferContainerReleaseDTO::getTransferContainerCode, Collectors.toSet())))
                .entrySet().stream()
                .flatMap(entry
                        -> transferContainerRepository.findAllByWarehouseCodeAndContainerCodeIn(entry.getKey(), entry.getValue()).stream())
                .filter(transferContainer -> TransferContainerStatusEnum.LOCKED == transferContainer.getTransferContainerStatus())
                .toList();
        if (CollectionUtils.isEmpty(lockedTransferContainers)) {
            log.info("Cannot find locked transfer container by request dto : {}", releaseDTOS);
            return;
        }

        transferContainerStockAggregate.releaseTransferContainers(lockedTransferContainers);
    }

}
