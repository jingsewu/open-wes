package org.openwes.wes.api.ems.proxy.dto;

import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collection;

@Data
@Accessors(chain = true)
public class CreateContainerTaskDTO implements Serializable {

    @NotEmpty
    private Long customerTaskId;

    @NotNull
    private BusinessTaskTypeEnum businessTaskType;

    @NotNull
    private ContainerTaskTypeEnum containerTaskType;

    private String taskCode;

    private String taskGroupCode;

    @NotNull
    private Integer taskPriority;

    @NotNull
    private Integer taskGroupPriority;

    @NotEmpty
    private String containerCode;
    private String containerFace;
    private String containerSpecCode;

    private String startLocation;
    private Collection<String> destinations;
}
