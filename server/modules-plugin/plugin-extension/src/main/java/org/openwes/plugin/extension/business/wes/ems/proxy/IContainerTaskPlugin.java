package org.openwes.plugin.extension.business.wes.ems.proxy;

import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;

public interface IContainerTaskPlugin extends IPlugin, IEntityLifecycleListener<Long, String> {
    default String getEntityName() {
        return "ContainerTask";
    }
}
