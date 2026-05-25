package org.openwes.station.controller.view.handler.impl.outbound;

import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.ContainerAreaHandler;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.springframework.stereotype.Service;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Service
public class OutboundContainerAreaHandler extends ContainerAreaHandler<OutboundWorkStationCache> {

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.OUTBOUND_CONTAINER_AREA;
    }
}
