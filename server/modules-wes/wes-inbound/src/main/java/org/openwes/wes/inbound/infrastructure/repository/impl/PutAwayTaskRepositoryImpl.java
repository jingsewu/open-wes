package org.openwes.wes.inbound.infrastructure.repository.impl;

import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.repository.PutAwayTaskRepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.PutAwayTaskDetailPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.PutAwayTaskPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.po.PutAwayTaskDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.PutAwayTaskPO;
import org.openwes.wes.inbound.infrastructure.persistence.transfer.PutAwayTaskPOTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PutAwayTaskRepositoryImpl implements PutAwayTaskRepository {

    private final PutAwayTaskPORepository putAwayTaskPORepository;
    private final PutAwayTaskDetailPORepository putAwayTaskDetailPORepository;
    private final PutAwayTaskPOTransfer putAwayTaskPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PutAwayTask saveOrderAndDetail(PutAwayTask putAwayTask) {
        PutAwayTaskPO putAwayTaskPO = putAwayTaskPORepository.save(putAwayTaskPOTransfer.toPO(putAwayTask));
        if (CollectionUtils.isEmpty(putAwayTask.getPutAwayTaskDetails())) {
            return putAwayTask;
        }
        List<PutAwayTaskDetailPO> putAwayTaskDetails = putAwayTask.getPutAwayTaskDetails()
                .stream()
                .peek(detail -> detail.setPutAwayTaskId(putAwayTaskPO.getId()))
                .map(putAwayTaskPOTransfer::toDetailPO)
                .toList();
        putAwayTaskDetailPORepository.saveAll(putAwayTaskDetails);

        return putAwayTaskPOTransfer.toDO(putAwayTaskPO, putAwayTaskDetails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PutAwayTask> saveAllOrdersAndDetails(List<PutAwayTask> putAwayTasks) {
        return putAwayTasks.stream().map(this::saveOrderAndDetail).toList();
    }

    @Override
    public List<PutAwayTask> findAllByTaskNoIn(Collection<String> taskNos) {
        List<PutAwayTaskPO> putAwayTaskPOS = putAwayTaskPORepository.findAllByTaskNoIn(taskNos);
        return putAwayTaskPOTransfer.toDOs(putAwayTaskPOS);
    }

    @Override
    public void saveAllOrders(List<PutAwayTask> putAwayTasks) {
        putAwayTaskPORepository.saveAll(putAwayTaskPOTransfer.toPOs(putAwayTasks));
    }

    @Override
    public List<PutAwayTask> findAllByIds(Collection<Long> putAwayTaskIds) {
        return putAwayTaskPOTransfer.toDOs(putAwayTaskPORepository.findAllById(putAwayTaskIds));
    }
}
