package org.openwes.wes.ems.proxy.domain.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.ems.proxy.domain.entity.ContainerTaskAndBusinessTaskRelation;
import org.openwes.wes.ems.proxy.domain.service.ContainerTaskService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContainerTaskServiceImpl implements ContainerTaskService {

    @Override
    public List<ContainerTask> groupContainerTasks(List<CreateContainerTaskDTO> createContainerTaskDTOS) {

        List<ContainerTask> containerTasks = Lists.newArrayList();
        createContainerTaskDTOS.stream().collect(Collectors.groupingBy(v ->
                        v.getContainerCode() + v.getContainerFace() + Sets.newTreeSet(Optional.ofNullable(v.getDestinations())
                                .orElseGet(ArrayList::new))))
                .forEach((key, values) -> containerTasks.addAll(this.generateTasks(values)));
        return containerTasks;
    }

    /**
     * through equipment structure to generate container task. like there are ACR and AMR ,
     * so maybe we should create two tasks for each ACR and AMR.
     * <p>
     * Simple implementation now just one for one
     *
     * @param createContainerTasks
     * @return
     */
    private List<ContainerTask> generateTasks(List<CreateContainerTaskDTO> createContainerTasks) {

        ContainerTask containerTask = new ContainerTask();

        List<ContainerTaskAndBusinessTaskRelation> relations = Lists.newArrayList();
        List<Long> customerTaskIds = Lists.newArrayList();
        int maxPriority = 0;
        for (CreateContainerTaskDTO createContainerTaskDTO : createContainerTasks) {
            customerTaskIds.add(createContainerTaskDTO.getCustomerTaskId());
            ContainerTaskAndBusinessTaskRelation relation = new ContainerTaskAndBusinessTaskRelation();
            relation.setCustomerTaskId(createContainerTaskDTO.getCustomerTaskId());
            relation.setTaskCode(containerTask.getTaskCode());
            relation.setTaskId(containerTask.getId());
            relations.add(relation);
            if (maxPriority < createContainerTaskDTO.getTaskPriority()) {
                maxPriority = createContainerTaskDTO.getTaskPriority();
            }
        }

        CreateContainerTaskDTO next = createContainerTasks.iterator().next();
        containerTask.setContainerCode(next.getContainerCode())
                .setContainerTaskType(next.getContainerTaskType())
                .setBusinessTaskType(next.getBusinessTaskType())
                .setContainerFace(next.getContainerFace())
                .setDestinations(next.getDestinations())
                .setCustomerTaskIds(customerTaskIds)
                .setTaskPriority(maxPriority)
                .setParentContainerTaskId(next.getCustomerTaskId())
                .setTaskGroupCode(next.getTaskGroupCode())
                .setTaskGroupPriority(next.getTaskGroupPriority())
                .setStartLocation(next.getStartLocation())
                .setRelations(relations)
                .setContainerSpecCode(next.getContainerSpecCode());
        return Lists.newArrayList(containerTask);
    }

    @Override
    public void doBeforeFinishContainerTasks(List<ContainerTask> containerTasks) {


    }

    @Override
    public void doAfterFinishContainerTasks(List<ContainerTask> containerTasks) {

    }

    @Override
    public List<ContainerTask> flatContainerTasks(List<ContainerTask> containerTasks) {

        List<ContainerTask> newContainerTasks = Lists.newArrayList(containerTasks);

        newContainerTasks.forEach(this::setParentId);
        newContainerTasks.addAll(newContainerTasks.stream()
                .flatMap(containerTask -> flat(containerTask, Lists.newArrayList()).stream()).toList());
        return newContainerTasks;
    }

    private List<ContainerTask> flat(ContainerTask parentContainerTask, List<ContainerTask> containerTasks) {

        if (CollectionUtils.isEmpty(parentContainerTask.getNextContainerTasks())) {
            return Collections.emptyList();
        }

        containerTasks.addAll(parentContainerTask.getNextContainerTasks());
        return parentContainerTask.getNextContainerTasks().stream()
                .flatMap(containerTask -> flat(containerTask, containerTasks).stream()).toList();
    }

    private void setParentId(ContainerTask parentContainerTask) {
        List<ContainerTask> nextContainerTasks = parentContainerTask.getNextContainerTasks();
        if (CollectionUtils.isEmpty(nextContainerTasks)) {
            return;
        }

        nextContainerTasks.forEach(containerTask -> {
            containerTask.setParentContainerTaskId(parentContainerTask.getId());
            setParentId(containerTask);
        });
    }
}
