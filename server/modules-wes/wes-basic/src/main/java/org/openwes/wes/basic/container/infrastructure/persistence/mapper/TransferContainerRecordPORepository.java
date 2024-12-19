package org.openwes.wes.basic.container.infrastructure.persistence.mapper;

import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;
import org.openwes.wes.basic.container.infrastructure.persistence.po.TransferContainerRecordPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferContainerRecordPORepository extends JpaRepository<TransferContainerRecordPO, Long> {

    List<TransferContainerRecordPO> findByTransferContainerCodeAndPickingOrderIdAndTransferContainerStatus(
            String transferContainerCode, Long pickingOrderId, TransferContainerRecordStatusEnum transferContainerRecordStatus);

    List<TransferContainerRecordPO> findAllByPickingOrderId(Long pickingOrderId);
}
