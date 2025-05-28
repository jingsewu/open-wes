package com.open.wes.extension.business.wes.ems.proxy.action;

import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;

import java.util.List;

public interface IContainerTaskGeneratorAction {

    default List<ContainerTaskDTO> generateContainerTasks(List<CreateContainerTaskDTO> createContainerTaskDTOs) {
        return null;
    }

}
