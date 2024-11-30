package org.openwes.wes.task.infrastructure.repository.impl;

import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import org.openwes.wes.task.infrastructure.persistence.mapper.TransferContainerPORepository;
import org.openwes.wes.task.infrastructure.persistence.po.TransferContainerPO;
import org.openwes.wes.task.infrastructure.persistence.transfer.TransferContainerPOTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferContainerRepositoryImpl implements TransferContainerRepository {

    private final TransferContainerPORepository transferContainerPORepository;
    private final TransferContainerPOTransfer transferContainerPOTransfer;

    @Override
    public void save(TransferContainer transferContainer) {
        transferContainerPORepository.save(transferContainerPOTransfer.toPO(transferContainer));
    }

    @Override
    public TransferContainer findByContainerCodeAndWarehouseCode(String containerCode, String warehouseCode) {
        return transferContainerPOTransfer.toDO(transferContainerPORepository.findByTransferContainerCodeAndWarehouseCode(containerCode, warehouseCode));
    }

    @Override
    public List<TransferContainer> findAllByWarehouseCodeAndContainerCodeIn(String warehouseCode, Collection<String> containerCodes) {
        return transferContainerPOTransfer.toDOs(transferContainerPORepository.findByWarehouseCodeAndTransferContainerCodeIn(warehouseCode, containerCodes));
    }

    @Override
    public List<TransferContainer> findAllLockedContainers(int limitDays) {
        Date date = DateUtils.addDays(new Date(), -limitDays);
        List<TransferContainerPO> transferContainerPOS = transferContainerPORepository
            .findAllByTransferContainerStatusAndUpdateTimeAfter(TransferContainerStatusEnum.LOCKED, date.getTime());
        return transferContainerPOTransfer.toDOs(transferContainerPOS);
    }

    @Override
    public void saveAll(List<TransferContainer> transferContainers) {
        transferContainerPORepository.saveAll(transferContainerPOTransfer.toPOs(transferContainers));
    }
}
