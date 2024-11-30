package org.openwes.wes.task.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.task.domain.entity.OperationTask;
import org.openwes.wes.task.domain.repository.OperationTaskRepository;
import org.openwes.wes.task.infrastructure.persistence.mapper.OperationTaskPORepository;
import org.openwes.wes.task.infrastructure.persistence.po.OperationTaskPO;
import org.openwes.wes.task.infrastructure.persistence.transfer.OperationTaskPOTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OperationTaskRepositoryImpl implements OperationTaskRepository {

    private final OperationTaskPORepository operationTaskPORepository;
    private final OperationTaskPOTransfer operationTaskPOTransfer;

    @Override
    public List<OperationTask> saveAll(List<OperationTask> operationTasks) {
        List<OperationTaskPO> operationTaskPOS = operationTaskPORepository.saveAll(operationTaskPOTransfer.toPOs(operationTasks));
        return operationTaskPOTransfer.toDOs(operationTaskPOS);
    }

    @Override
    public List<OperationTask> findAllUndoTasksByStationAndContainer(Long workStationId, String containerCode, String face, OperationTaskTypeEnum taskType) {
        List<OperationTaskPO> operationTaskPOS = operationTaskPORepository.findAllByTaskStatusInAndTaskTypeAndSourceContainerCodeAndSourceContainerFace(
                        Lists.newArrayList(OperationTaskStatusEnum.NEW, OperationTaskStatusEnum.PROCESSING), taskType, containerCode, face)
                .stream().filter(v -> Objects.equals(workStationId, v.getWorkStationId())
                        || (v.getAssignedStationSlot() != null && v.getAssignedStationSlot().containsKey(workStationId))).toList();

        return operationTaskPOTransfer.toDOs(operationTaskPOS);
    }

    @Override
    public Long countByTransferContainerRecordId(Long transferContainerRecordId) {
        return operationTaskPORepository.countByTransferContainerRecordId(transferContainerRecordId);
    }

    @Override
    public List<OperationTask> findAllByTransferContainerRecordIds(Collection<Long> transferContainerRecordIds) {
        List<OperationTaskPO> operationTaskPOS = operationTaskPORepository.findByTransferContainerRecordIdIn(transferContainerRecordIds);
        return operationTaskPOTransfer.toDOs(operationTaskPOS);
    }

    @Override
    public List<OperationTask> findAllByIds(Collection<Long> taskIds) {
        return operationTaskPOTransfer.toDOs(operationTaskPORepository.findAllById(taskIds));
    }

    @Override
    public List<OperationTask> findAllByLimitDaysAndWarehouseCodeAndCode(int days, String warehouseCode, OperationTaskTypeEnum taskType, int limitCount) {
        Date date = DateUtils.addDays(new Date(), -days);
        Pageable pageable = Pageable.ofSize(limitCount);
        List<OperationTaskPO> operationTaskPOS =
                operationTaskPORepository.findAllByTaskStatusAndWarehouseCodeAndTaskTypeAndCreateTimeAfter(OperationTaskStatusEnum.NEW,
                        warehouseCode, taskType, date.getTime(), pageable);
        return operationTaskPOTransfer.toDOs(operationTaskPOS);
    }

    @Override
    public List<OperationTask> findAllByPickingOrderIds(Collection<Long> pickingOrderIds) {
        List<OperationTaskPO> operationTaskPOS = operationTaskPORepository.findAllByOrderIdIn(pickingOrderIds);
        return operationTaskPOTransfer.toDOs(operationTaskPOS);
    }

    @Override
    public List<OperationTask> findAllByLimitPickingOrderIds(Collection<Long> pickingOrderIds, int limitCount) {
        Pageable pageable = Pageable.ofSize(limitCount);
        List<OperationTaskPO> operationTaskPOS = operationTaskPORepository.findAllByOrderIdIn(pickingOrderIds, pageable);
        return operationTaskPOTransfer.toDOs(operationTaskPOS);
    }
}
