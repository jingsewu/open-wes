package org.openwes.wes.ems.proxy.domain.service;

import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;

import java.util.List;

public interface ContainerTaskService {

    /**
     * group by the same source container and destination
     *
     * @param createContainerTaskDTOS
     *
     * @return
     */
    List<ContainerTask> groupContainerTasks(List<CreateContainerTaskDTO> createContainerTaskDTOS);

    void doBeforeFinishContainerTasks(List<ContainerTask> containerTasks);

    void doAfterFinishContainerTasks(List<ContainerTask> containerTasks);
}
