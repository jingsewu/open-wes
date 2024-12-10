package org.openwes.wes.task.domain.repository;

import org.openwes.wes.task.domain.entity.TransferContainerRecord;

import java.util.List;

public interface TransferContainerRecordRepository {

    TransferContainerRecord save(TransferContainerRecord transferContainerRecord);

    void delete(Long id);

    TransferContainerRecord findById(Long transferContainerRecordId);

    List<TransferContainerRecord> findAllById(List<Long> currentPeriodRelateRecordIds);

    TransferContainerRecord findCurrentPickOrderTransferContainerRecord(Long pickingOrderId, String containerCode);

    List<TransferContainerRecord> findAllByPickingOrderId(Long pickingOrderId);
}
