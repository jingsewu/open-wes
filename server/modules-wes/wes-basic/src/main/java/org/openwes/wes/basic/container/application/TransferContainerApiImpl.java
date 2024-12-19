package org.openwes.wes.basic.container.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.wes.api.basic.ITransferContainerApi;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.openwes.wes.api.task.dto.*;
import org.openwes.wes.basic.container.domain.aggregate.TransferContainerPutWallAggregate;
import org.openwes.wes.basic.container.domain.entity.TransferContainer;
import org.openwes.wes.basic.container.domain.entity.TransferContainerRecord;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRecordRepository;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRepository;
import org.openwes.wes.basic.container.domain.service.TransferContainerService;
import org.openwes.wes.basic.container.domain.transfer.TransferContainerTransfer;
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
    private final TransferContainerRecordRepository transferContainerRecordRepository;
    private final TransferContainerService transferContainerService;
    private final TransferContainerPutWallAggregate transferContainerPutWallAggregate;
    private final TransferContainerTransfer transferContainerTransfer;

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

        lockedTransferContainers.forEach(TransferContainer::unlock);
        transferContainerRepository.saveAll(lockedTransferContainers);
    }

    @Override
    public void release(Long id) {
        TransferContainer transferContainer = transferContainerRepository.findById(id);
        transferContainer.unlock();
        transferContainerRepository.save(transferContainer);
    }

    @Override
    public TransferContainerDTO findByContainerCodeAndWarehouseCode(String containerCode, String warehouseCode) {
        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(containerCode, warehouseCode);
        return transferContainerTransfer.toDTO(transferContainer);
    }

    @Override
    public List<TransferContainerDTO> findAllByWarehouseCodeAndContainerCodeIn(String warehouseCode, List<String> transferContainerCodes) {
        List<TransferContainer> transferContainers = transferContainerRepository.findAllByWarehouseCodeAndContainerCodeIn(warehouseCode, transferContainerCodes);
        return transferContainerTransfer.toDTOs(transferContainers);
    }

    @Override
    public void bindContainer(BindContainerDTO bindContainerDTO) {
        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(bindContainerDTO.getContainerCode(), bindContainerDTO.getWarehouseCode());
        transferContainerService.validateBindContainer(transferContainer);

        transferContainerPutWallAggregate.bindContainer(bindContainerDTO, transferContainer, bindContainerDTO.getPickingOrderId());
    }

    @Override
    public void unBindContainer(UnBindContainerDTO unBindContainerDTO, Long transferContainerRecordId) {

        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(unBindContainerDTO.getContainerCode(), unBindContainerDTO.getWarehouseCode());

        transferContainerPutWallAggregate.unBindContainer(unBindContainerDTO, transferContainer, transferContainerRecordId);
    }

    @Override
    public void sealContainer(SealContainerDTO sealContainerDTO) {
        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository
                .findCurrentPickOrderTransferContainerRecord(sealContainerDTO.getPickingOrderId(), sealContainerDTO.getTransferContainerCode());

        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(transferContainerRecord.getTransferContainerCode(), sealContainerDTO.getWarehouseCode());

        transferContainerPutWallAggregate.sealContainer(sealContainerDTO.isNeedHandlePutWallSlot(), transferContainerRecord, transferContainer);
    }

    @Override
    public void sealContainer(Long pickingOrderId) {
        List<TransferContainerRecord> transferContainerRecords = transferContainerRecordRepository.findAllBoundedRecordsByPickingOrderId(pickingOrderId);

        if (ObjectUtils.isEmpty(transferContainerRecords)) {
            log.info("picking order: {} can't find andy transfer container records", pickingOrderId);
            return;
        }

        String warehouseCode = transferContainerRecords.iterator().next().getWarehouseCode();
        List<String> transferContainerCodes = transferContainerRecords.stream().map(TransferContainerRecord::getTransferContainerCode).toList();
        List<TransferContainer> transferContainers = transferContainerRepository
                .findAllByWarehouseCodeAndContainerCodeIn(warehouseCode, transferContainerCodes);

        transferContainerPutWallAggregate.sealContainer(false, transferContainerRecords, transferContainers);
    }

}
