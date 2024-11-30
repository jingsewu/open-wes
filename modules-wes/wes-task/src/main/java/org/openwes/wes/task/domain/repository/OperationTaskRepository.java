package org.openwes.wes.task.domain.repository;

import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.task.domain.entity.OperationTask;

import java.util.Collection;
import java.util.List;

public interface OperationTaskRepository {

    List<OperationTask> saveAll(List<OperationTask> operationTasks);

    List<OperationTask> findAllUndoTasksByStationAndContainer(Long workStationId, String containerCode, String face, OperationTaskTypeEnum taskType);

    Long countByTransferContainerRecordId(Long transferContainerRecordId);

    List<OperationTask> findAllByTransferContainerRecordIds(Collection<Long> transferContainerRecordIds);

    List<OperationTask> findAllByIds(Collection<Long> taskIds);

    List<OperationTask> findAllByLimitDaysAndWarehouseCodeAndCode(int days, String warehouseCode, OperationTaskTypeEnum taskType, int limitCount);

    List<OperationTask> findAllByPickingOrderIds(Collection<Long> pickingOrderIds);

    List<OperationTask> findAllByLimitPickingOrderIds(Collection<Long> pickingOrderIds, int limitCount);
}
