package com.open.wes.extension.wes.ems.proxy;

import com.open.wes.extension.IPlugin;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;

import java.util.List;

public interface IContainerTaskPlugin extends IContainerTaskGeneratorAction, IPlugin {

    default IContainerTaskGeneratorAction getGeneratorPlugin() {
        return null;
    }

    default void beforeCreate(List<CreateContainerTaskDTO> createContainerTaskDTOs) {

    }

    default void afterCreate(List<CreateContainerTaskDTO> createContainerTaskDTOs) {

    }

    default void beforeFinish(List<ContainerTaskDTO> updateContainerTaskDTOs) {

    }

    default void afterFinish(List<ContainerTaskDTO> updateContainerTaskDTOs) {

    }

    default void beforeCancel(List<ContainerTaskDTO> containerTaskDTOs) {

    }

    default void afterCancel(List<ContainerTaskDTO> containerTaskDTOs) {

    }

}
