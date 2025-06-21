package org.openwes.plugin.example;

import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.extension.business.wes.outbound.IOutboundWavePlugin;
import org.pf4j.Extension;

@Slf4j
@Extension
public class OutboundWaveLifecyclePlugin implements IOutboundWavePlugin {
    @Override
    public void afterStatusChange(Long aLong, String orderNo, String newStatus) {
        log.info("Outbound wave: {} order no: {} status changed to {} ", aLong, orderNo, newStatus);
    }
}
