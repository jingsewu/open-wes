package org.openwes.wes.basic.container.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.wes.api.basic.ITransferContainerRecordApi;
import org.openwes.wes.api.basic.dto.TransferContainerRecordDTO;
import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;
import org.openwes.wes.basic.container.domain.entity.TransferContainerRecord;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRecordRepository;
import org.openwes.wes.basic.container.domain.transfer.TransferContainerRecordTransfer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Primary
@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class TransferContainerRecordApiImpl implements ITransferContainerRecordApi {

    private final TransferContainerRecordRepository transferContainerRecordRepository;
    private final TransferContainerRecordTransfer transferContainerRecordTransfer;

    @Override
    public TransferContainerRecordDTO findCurrentPickOrderTransferContainerRecord(Long pickingOrderId, String containerCode) {
        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository.findCurrentPickOrderTransferContainerRecord(pickingOrderId, containerCode);
        return transferContainerRecordTransfer.toDTO(transferContainerRecord);
    }

    @Override
    public List<TransferContainerRecordDTO> findAllBoundedRecordsByPickingOrderId(Long pickingOrderId) {
        List<TransferContainerRecord> transferContainerRecords = transferContainerRecordRepository.findAllByPickingOrderId(pickingOrderId).stream().filter(v -> v.getTransferContainerStatus() == TransferContainerRecordStatusEnum.BOUNDED)
                .toList();
        return transferContainerRecordTransfer.toDTOs(transferContainerRecords);
    }

    @Override
    public TransferContainerRecordDTO findById(Long transferContainerRecordId) {
        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository.findById(transferContainerRecordId);
        return transferContainerRecordTransfer.toDTO(transferContainerRecord);
    }

    @Override
    public List<TransferContainerRecordDTO> findAllById(List<Long> currentPeriodRelateRecordIds) {
        List<TransferContainerRecord> transferContainerRecords = transferContainerRecordRepository.findAllById(currentPeriodRelateRecordIds);
        return transferContainerRecordTransfer.toDTOs(transferContainerRecords);
    }
}
