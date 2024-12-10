package org.openwes.wes.api.ems.proxy.dto;

import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskStatusEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Data
public class ContainerTaskDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4494134284167556236L;

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

    private ContainerTaskStatusEnum taskStatus;

    private Long parentContainerTaskId;

    private List<ContainerTaskDTO> nextContainerTasks;

}
