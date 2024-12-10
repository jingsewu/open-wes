package org.openwes.wes.api.ems.proxy;


import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;
import java.util.List;

public interface IContainerTaskApi {

    void createContainerTasks(@NotEmpty List<CreateContainerTaskDTO> createContainerTasks);

    void updateContainerTaskStatus(@NotEmpty List<UpdateContainerTaskDTO> updateContainerTasks);

    void cancel(@NotEmpty Collection<String> taskCodes);

    void cancel(@NotEmpty List<Long> customerTaskIds);
}
