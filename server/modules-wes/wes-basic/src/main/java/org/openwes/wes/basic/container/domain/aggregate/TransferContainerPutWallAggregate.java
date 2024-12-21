package org.openwes.wes.basic.container.domain.aggregate;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.openwes.wes.api.task.dto.BindContainerDTO;
import org.openwes.wes.api.task.dto.SealContainerDTO;
import org.openwes.wes.api.task.dto.UnBindContainerDTO;
import org.openwes.wes.basic.container.domain.entity.TransferContainer;
import org.openwes.wes.basic.container.domain.entity.TransferContainerRecord;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRecordRepository;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferContainerPutWallAggregate {

    private final IPutWallApi putWallApi;
    private final TransferContainerRecordRepository transferContainerRecordRepository;
    private final TransferContainerRepository transferContainerRepository;

    @Transactional(rollbackFor = Exception.class)
    public void bindContainer(BindContainerDTO bindContainerDTO, TransferContainer transferContainer, Long pickingOrderId) {

        TransferContainerRecord transferContainerRecord = new TransferContainerRecord(bindContainerDTO, pickingOrderId);
        TransferContainerRecord saved = transferContainerRecordRepository.save(transferContainerRecord);

        if (transferContainer == null) {
            transferContainer = new TransferContainer()
                    .setTransferContainerCode(bindContainerDTO.getContainerCode())
                    .setTransferContainerStatus(TransferContainerStatusEnum.IDLE)
                    .setWarehouseCode(bindContainerDTO.getWarehouseCode());
        }

        transferContainer.occupy(saved.getId());
        transferContainerRepository.save(transferContainer);

        if (bindContainerDTO.isNeedHandlePutWallSlot()) {
            putWallApi.bindContainer(bindContainerDTO, saved.getId());
        }
    }

    @Transactional
    public void unBindContainer(UnBindContainerDTO unBindContainerDTO, TransferContainer transferContainer, Long transferContainerRecord) {

        transferContainer.unOccupy();
        transferContainerRepository.save(transferContainer);

        transferContainerRecordRepository.delete(transferContainerRecord);

        if (unBindContainerDTO.isNeedHandlePutWallSlot()) {
            putWallApi.unBindContainer(unBindContainerDTO);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void sealContainer(SealContainerDTO sealContainerDTO, TransferContainerRecord transferContainerRecord,
                              TransferContainer transferContainer) {

        //1. put wall slot seal container
        if (sealContainerDTO.isNeedHandlePutWallSlot()) {
            if (sealContainerDTO.isPickingOrderCompleted()) {
                putWallApi.sealContainer(transferContainerRecord.getPutWallSlotCode(), transferContainerRecord.getWorkStationId());
            } else {
                putWallApi.splitContainer(transferContainerRecord.getPutWallSlotCode(), transferContainerRecord.getWorkStationId());
            }
        }

        //2. save transfer container record
        transferContainerRecord.seal();
        transferContainerRecordRepository.save(transferContainerRecord);

        //3. save transfer container
        transferContainer.lock();
        transferContainerRepository.save(transferContainer);
    }

    @Transactional(rollbackFor = Exception.class)
    public void sealContainer(boolean isNeedHandlePutWallSlot, List<TransferContainerRecord> transferContainerRecords,
                              List<TransferContainer> transferContainers) {

        //1. put wall slot seal container
        if (isNeedHandlePutWallSlot) {
            transferContainerRecords.forEach(transferContainerRecord -> {
                putWallApi.sealContainer(transferContainerRecord.getPutWallSlotCode(), transferContainerRecord.getWorkStationId());
            });
        }

        //2. save transfer container record
        transferContainerRecords.forEach(transferContainerRecord -> {
            transferContainerRecord.seal();
            transferContainerRecordRepository.save(transferContainerRecord);
        });

        //3. save transfer container
        transferContainers.forEach(transferContainer -> {
            transferContainer.lock();
            transferContainerRepository.save(transferContainer);
        });
    }
}

