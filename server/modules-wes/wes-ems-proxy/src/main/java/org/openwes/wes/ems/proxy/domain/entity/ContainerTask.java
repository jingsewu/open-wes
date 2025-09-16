package org.openwes.wes.ems.proxy.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.plugin.api.dto.event.LifeCycleStatusChangeEvent;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskAndBusinessTaskRelationStatusEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskStatusEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@Slf4j
public class ContainerTask extends AggregatorRoot implements Serializable {

    private Long id;

    private BusinessTaskTypeEnum businessTaskType;
    private ContainerTaskTypeEnum containerTaskType;

    private String taskCode;
    private String taskGroupCode;

    private Integer taskPriority;
    private Integer taskGroupPriority;

    private String containerCode;
    private String containerFace;

    private String startLocation;
    private String containerSpecCode;
    private Collection<String> destinations;

    private List<Long> customerTaskIds;

    private List<ContainerTaskAndBusinessTaskRelation> relations;

    private ContainerTaskStatusEnum taskStatus;

    private Long parentContainerTaskId;

    private List<ContainerTask> nextContainerTasks;

    // receive from wcs
    private String finalDestination;

    public void initialize() {
        this.taskCode = OrderNoGenerator.generationContainerTaskCode();
        this.taskStatus = ContainerTaskStatusEnum.NEW;
        this.id = IdGenerator.generateId();

        this.addAsynchronousDomainEvents(new LifeCycleStatusChangeEvent(this.taskStatus.name(),
                this.id, this.getClass().getSimpleName()));
    }

    public void updateTaskStatus(ContainerTaskStatusEnum taskStatus, String locationCode) {

        log.info("container task id: {} task code: {} update task status to: {}", this.id, this.taskCode, taskStatus);

        this.finalDestination = locationCode;
        if ((ContainerTaskTypeEnum.INBOUND == this.containerTaskType
                || BusinessTaskTypeEnum.EMPTY_CONTAINER_OUTBOUND == this.businessTaskType)
                && ContainerTaskStatusEnum.WCS_SUCCEEDED == taskStatus) {
            this.taskStatus = ContainerTaskStatusEnum.COMPLETED;
        } else {
            this.taskStatus = taskStatus;
        }

        this.addAsynchronousDomainEvents(new LifeCycleStatusChangeEvent(this.taskStatus.name(),
                this.id, this.getClass().getSimpleName()));

        if (CollectionUtils.isEmpty(relations)) {
            return;
        }

        if (ContainerTaskStatusEnum.COMPLETED == this.taskStatus) {
            this.relations.stream()
                    .filter(relation -> ContainerTaskAndBusinessTaskRelationStatusEnum.processingStates.contains(relation.getContainerTaskAndBusinessTaskRelationStatus()))
                    .forEach(ContainerTaskAndBusinessTaskRelation::complete);
        } else if (ContainerTaskStatusEnum.CANCELED == this.taskStatus) {
            this.relations.stream()
                    .filter(relation -> ContainerTaskAndBusinessTaskRelationStatusEnum.processingStates.contains(relation.getContainerTaskAndBusinessTaskRelationStatus()))
                    .forEach(ContainerTaskAndBusinessTaskRelation::cancel);
        }
    }

    public boolean cancel() {

        log.info("container task id: {} task code: {} cancel", this.id, this.taskCode);

        if (ContainerTaskStatusEnum.processingStates.contains(this.taskStatus)) {
            this.taskStatus = ContainerTaskStatusEnum.CANCELED;
            this.relations.forEach(ContainerTaskAndBusinessTaskRelation::cancel);

            this.addAsynchronousDomainEvents(new LifeCycleStatusChangeEvent(this.taskStatus.name(),
                    this.id, this.getClass().getSimpleName()));

            return true;
        }
        return false;
    }

    public void improvePriority(Integer priority) {
        log.info("container task id: {} task code: {} improve priority: {}", this.id, this.taskCode, priority);

        if (this.taskPriority >= priority) {
            return;
        }
        this.taskPriority = priority;
    }
}
