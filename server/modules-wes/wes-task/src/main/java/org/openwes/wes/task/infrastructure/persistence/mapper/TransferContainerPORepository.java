package org.openwes.wes.task.infrastructure.persistence.mapper;

import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.openwes.wes.task.infrastructure.persistence.po.TransferContainerPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransferContainerPORepository extends JpaRepository<TransferContainerPO, Long> {

    TransferContainerPO findByTransferContainerCodeAndWarehouseCode(String containerCode, String warehouseCode);

    List<TransferContainerPO> findByWarehouseCodeAndTransferContainerCodeIn(String warehouseCode, Collection<String> containerCodes);

    List<TransferContainerPO> findAllByTransferContainerStatusAndUpdateTimeAfter(TransferContainerStatusEnum status, long updateTime);
}
