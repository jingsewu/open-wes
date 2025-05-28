package com.open.wes.extension.business.wes.outbound;

import com.open.wes.extension.IPlugin;
import com.open.wes.extension.business.IEntityLifecycleListener;

public interface IOutboundWavePlugin extends IPlugin, IEntityLifecycleListener {

    default String getEntityName() {
        return "OutboundWave";
    }
}
