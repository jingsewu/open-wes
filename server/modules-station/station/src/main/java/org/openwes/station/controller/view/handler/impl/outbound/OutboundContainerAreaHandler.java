package org.openwes.station.controller.view.handler.impl.outbound;

import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.ContainerAreaHandler;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.transfer.ArriveContainerCacheTransfer;
import org.springframework.stereotype.Service;

@Service
public class OutboundContainerAreaHandler extends ContainerAreaHandler<OutboundWorkStationCache> {

    public OutboundContainerAreaHandler(ArriveContainerCacheTransfer arriveContainerCacheTransfer) {
        super(arriveContainerCacheTransfer);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.OUTBOUND_CONTAINER_AREA;
    }
}
