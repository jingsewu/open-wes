package org.openwes.wes.ems.proxy.domain.repository;

import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;

import java.util.Collection;
import java.util.List;

public interface ContainerTaskRepository {

    void saveAll(List<ContainerTask> containerTasks);

    List<ContainerTask> findAllByTaskCodes(Collection<String> taskCodes);

    List<ContainerTask> findAllByCustomerTaskIds(List<Long> customerTaskIds);
}
