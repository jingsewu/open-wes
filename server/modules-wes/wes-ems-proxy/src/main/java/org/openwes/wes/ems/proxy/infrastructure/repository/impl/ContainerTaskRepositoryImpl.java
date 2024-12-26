package org.openwes.wes.ems.proxy.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.ems.proxy.domain.repository.ContainerTaskAndBusinessTaskRelation;
import org.openwes.wes.ems.proxy.domain.repository.ContainerTaskRepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.mapper.ContainerTaskAndBusinessTaskRelationPORepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.mapper.ContainerTaskPORepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskAndBusinessTaskRelationPO;
import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskPO;
import org.openwes.wes.ems.proxy.infrastructure.persistence.transfer.ContainerTaskAndBusinessTaskRelationPOTransfer;
import org.openwes.wes.ems.proxy.infrastructure.persistence.transfer.ContainerTaskPOTransfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContainerTaskRepositoryImpl implements ContainerTaskRepository {

    private final ContainerTaskPORepository containerTaskPORepository;
    private final ContainerTaskPOTransfer containerTaskPOTransfer;
    private final ContainerTaskAndBusinessTaskRelationPORepository containerTaskAndBusinessTaskRelationPORepository;
    private final ContainerTaskAndBusinessTaskRelationPOTransfer containerTaskAndBusinessTaskRelationPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<ContainerTask> containerTasks) {

        List<ContainerTaskPO> containerTaskPOS = containerTaskPOTransfer.toPOs(containerTasks);
        containerTaskPORepository.saveAll(containerTaskPOS);

        List<ContainerTaskAndBusinessTaskRelation> relations = containerTasks.stream()
                .filter(task -> CollectionUtils.isNotEmpty(task.getRelations()))
                .flatMap(task -> task.getRelations().stream()).toList();

        List<ContainerTaskAndBusinessTaskRelationPO> relationPOS = containerTaskAndBusinessTaskRelationPOTransfer.toPOs(relations);
        containerTaskAndBusinessTaskRelationPORepository.saveAll(relationPOS);
    }

    @Override
    public List<ContainerTask> findAllByTaskCodes(Collection<String> taskCodes) {
        List<ContainerTaskPO> containerTaskPOS = containerTaskPORepository.findAllByTaskCodeIn(taskCodes);
        return containerTaskPOTransfer.toDOs(containerTaskPOS);
    }

    @Override
    public List<ContainerTask> findAllByCustomerTaskIds(List<Long> customerTaskIds) {
        List<ContainerTaskAndBusinessTaskRelationPO> relationPOs = containerTaskAndBusinessTaskRelationPORepository
                .findByCustomerTaskIdIn(customerTaskIds);

        List<Long> containerTaskIds = relationPOs.stream().map(ContainerTaskAndBusinessTaskRelationPO::getTaskId).toList();
        List<ContainerTaskPO> containerTaskPOS = containerTaskPORepository.findAllById(containerTaskIds);

        Map<Long, List<ContainerTaskAndBusinessTaskRelationPO>> relationMap = relationPOs.stream().collect(Collectors.groupingBy(ContainerTaskAndBusinessTaskRelationPO::getTaskId));

        return containerTaskPOS.stream().map(containerTaskPO ->
                containerTaskPOTransfer.toDO(containerTaskPO, relationMap.get(containerTaskPO.getId()))).toList();
    }

}
