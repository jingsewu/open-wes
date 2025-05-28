package com.open.wes.extension.business.wes.ems.proxy;

import com.open.wes.extension.IPlugin;
import com.open.wes.extension.business.IEntityLifecycleListener;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;

import java.util.List;

public interface IContainerTaskPlugin extends IPlugin, IEntityLifecycleListener<List<CreateContainerTaskDTO>,
        List<ContainerTaskDTO>, ContainerTaskDTO> {
    default String getEntityName() {
        return "ContainerTask";
    }
}
