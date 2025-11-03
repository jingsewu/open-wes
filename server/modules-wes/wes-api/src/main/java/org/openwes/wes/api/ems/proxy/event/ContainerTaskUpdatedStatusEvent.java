package org.openwes.wes.api.ems.proxy.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskStatusEnum;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerTaskUpdatedStatusEvent extends DomainEvent {

    private List<Long> relationTaskIds;

    private ContainerTaskStatusEnum taskStatus;
    private String locationCode;
    private BusinessTaskTypeEnum businessTaskType;

    public ContainerTaskUpdatedStatusEvent(Long id, ContainerTaskStatusEnum taskStatus, String locationCode,
                                           List<Long> relationTaskIds,BusinessTaskTypeEnum businessTaskType) {
        super(id);
        this.taskStatus = taskStatus;
        this.locationCode = locationCode;
        this.relationTaskIds = relationTaskIds;
        this.businessTaskType = businessTaskType;
    }
}
