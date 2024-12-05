package org.openwes.wes.task.infrastructure.persistence.mapper;

import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.task.infrastructure.persistence.po.OperationTaskPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OperationTaskPORepository extends JpaRepository<OperationTaskPO, Long> {

    List<OperationTaskPO> findAllByTaskStatusInAndTaskTypeAndSourceContainerCodeAndSourceContainerFace(
            List<OperationTaskStatusEnum> taskStatuses,
            OperationTaskTypeEnum taskType,
            String containerCode, String face);


    List<OperationTaskPO> findAllByTaskStatusAndWarehouseCodeAndTaskTypeAndCreateTimeAfter(
            OperationTaskStatusEnum taskStatus,
            String warehouseCode,
            OperationTaskTypeEnum taskType, long createTime, Pageable limitCount);

    List<OperationTaskPO> findAllByOrderIdIn(Collection<Long> orderIds);

    List<OperationTaskPO> findAllByOrderIdIn(Collection<Long> orderIds, Pageable limitCount);

    List<OperationTaskPO> findByTransferContainerRecordIdIn(Collection<Long> transferContainerRecordId);

    Long countByTransferContainerRecordId(Long transferContainerRecordId);

}
