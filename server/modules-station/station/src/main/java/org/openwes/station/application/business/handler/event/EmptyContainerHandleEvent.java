package org.openwes.station.application.business.handler.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;

@Data
public class EmptyContainerHandleEvent {

    @NotEmpty
    private String containerCode;
    @NotNull
    private ContainerOperationTypeEnum containerOperationType;
}
