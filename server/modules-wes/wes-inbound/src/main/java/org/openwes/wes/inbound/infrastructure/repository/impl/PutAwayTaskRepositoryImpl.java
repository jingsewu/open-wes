package org.openwes.wes.inbound.infrastructure.repository.impl;

import org.openwes.domain.event.AggregatorRoot;
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
    public void saveOrderAndDetail(PutAwayTask putAwayTask) {

        putAwayTask.sendAndClearEvents();

        putAwayTaskPORepository.save(putAwayTaskPOTransfer.toPO(putAwayTask));
        if (CollectionUtils.isEmpty(putAwayTask.getPutAwayTaskDetails())) {
            return;
        }
        List<PutAwayTaskDetailPO> putAwayTaskDetails = putAwayTask.getPutAwayTaskDetails()
                .stream()
                .map(putAwayTaskPOTransfer::toDetailPO)
                .toList();
        putAwayTaskDetailPORepository.saveAll(putAwayTaskDetails);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAllOrdersAndDetails(List<PutAwayTask> putAwayTasks) {

        putAwayTasks.forEach(AggregatorRoot::sendAndClearEvents);

        List<PutAwayTaskPO> putAwayTaskPOS = putAwayTaskPOTransfer.toPOs(putAwayTasks);
        putAwayTaskPORepository.saveAll(putAwayTaskPOS);

        List<PutAwayTaskDetailPO> putAwayTaskDetailPOS = putAwayTasks.stream().flatMap(v -> v.getPutAwayTaskDetails().stream())
                .map(putAwayTaskPOTransfer::toDetailPO).toList();
        putAwayTaskDetailPORepository.saveAll(putAwayTaskDetailPOS);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAllOrders(List<PutAwayTask> putAwayTasks) {
        putAwayTasks.forEach(AggregatorRoot::sendAndClearEvents);
        putAwayTaskPORepository.saveAll(putAwayTaskPOTransfer.toPOs(putAwayTasks));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PutAwayTask> findAllByTaskNoIn(Collection<String> taskNos) {
        List<PutAwayTaskPO> putAwayTaskPOS = putAwayTaskPORepository.findAllByTaskNoIn(taskNos);
        return putAwayTaskPOTransfer.toDOs(putAwayTaskPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PutAwayTask> findAllByIds(Collection<Long> putAwayTaskIds) {
        return putAwayTaskPOTransfer.toDOs(putAwayTaskPORepository.findAllById(putAwayTaskIds));
    }
}
