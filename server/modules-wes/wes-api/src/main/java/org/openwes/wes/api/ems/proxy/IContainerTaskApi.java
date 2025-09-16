package org.openwes.wes.api.ems.proxy;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;

import java.util.Collection;
import java.util.List;

public interface IContainerTaskApi {

    List<ContainerTaskDTO> createContainerTasks(@NotEmpty List<CreateContainerTaskDTO> createContainerTasks);

    void updateContainerTaskStatus(@NotEmpty List<UpdateContainerTaskDTO> updateContainerTasks);

    void cancel(@NotEmpty Collection<String> taskCodes);

    void cancel(@NotEmpty List<Long> customerTaskIds);

    void improvePriority(List<Long> customerTaskIds, @NotNull Integer priority);
}
