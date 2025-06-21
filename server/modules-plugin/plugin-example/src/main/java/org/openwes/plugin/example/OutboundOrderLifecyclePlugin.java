package org.openwes.plugin.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.extension.business.wes.outbound.IOutboundPlanOrderPlugin;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.pf4j.Extension;

import java.util.List;

@Slf4j
@Extension
@RequiredArgsConstructor
public class OutboundOrderLifecyclePlugin implements IOutboundPlanOrderPlugin {

    private final IOutboundPlanOrderApi outboundPlanOrderApi;

    @Override
    public void afterStatusChange(Long id, String orderNo, String newStatus) {
        log.info("Outbound order: {} order no: {} status changed to {} ", id, orderNo, newStatus);
        if (id != null) {
            log.info("outbound order size: {}", outboundPlanOrderApi.getByIds(List.of(id)).size());
        }
    }
}
