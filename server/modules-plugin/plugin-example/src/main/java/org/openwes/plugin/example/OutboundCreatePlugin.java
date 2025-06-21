package org.openwes.plugin.example;

import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.api.platform.request.IOutboundPlanOrderCreatePlugin;
import org.pf4j.Extension;

@Slf4j
@Extension
public class OutboundCreatePlugin implements IOutboundPlanOrderCreatePlugin {

    @Override
    public void validate(RequestHandleContext context) {
        log.info("outbound validate");
    }

    @Override
    public void supply(RequestHandleContext context) {
        log.info("outbound supply");
    }

    @Override
    public void afterInvoke(RequestHandleContext context) {
        log.info("outbound afterInvoke");
    }
}
