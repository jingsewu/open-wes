package org.openwes.plugin.extension.business.wes.outbound;

import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;

public interface IOutboundPlanOrderPlugin extends IPlugin, IEntityLifecycleListener<Long, String> {

    default String getEntityName() {
        return "OutboundPlanOrder";
    }
}
