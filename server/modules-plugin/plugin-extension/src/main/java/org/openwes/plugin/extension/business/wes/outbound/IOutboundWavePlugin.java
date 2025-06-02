package org.openwes.plugin.extension.business.wes.outbound;

import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;

public interface IOutboundWavePlugin extends IPlugin, IEntityLifecycleListener<Long> {

    default String getEntityName() {
        return "OutboundWave";
    }
}
