package org.openwes.wes.ems.proxy.domain.repository;

import org.openwes.wes.api.ems.proxy.constants.ContainerTaskAndBusinessTaskRelationStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Accessors(chain = true)
@Data
public class ContainerTaskAndBusinessTaskRelation implements Serializable {

    private Long id;

    private Long taskId;

    private String taskCode;

    private Long customerTaskId;

    private ContainerTaskAndBusinessTaskRelationStatusEnum containerTaskAndBusinessTaskRelationStatus = ContainerTaskAndBusinessTaskRelationStatusEnum.NEW;

    public void complete() {
        log.info("container task business relation: {} completed, taskId: {}, taskCode: {}", this.id, this.taskId, this.taskCode);
        this.containerTaskAndBusinessTaskRelationStatus = ContainerTaskAndBusinessTaskRelationStatusEnum.COMPLETED;
    }

    public void cancel() {
        log.info("container task business relation: {} canceled, taskId: {}, taskCode: {}", this.id, this.taskId, this.taskCode);
        this.containerTaskAndBusinessTaskRelationStatus = ContainerTaskAndBusinessTaskRelationStatusEnum.CANCELED;
    }
}
