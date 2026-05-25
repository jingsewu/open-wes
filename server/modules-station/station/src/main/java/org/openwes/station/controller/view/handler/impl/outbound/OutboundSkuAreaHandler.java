package org.openwes.station.controller.view.handler.impl.outbound;

import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.handler.SkuAreaHandler;
import org.springframework.stereotype.Service;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Service
public class OutboundSkuAreaHandler extends SkuAreaHandler<OutboundWorkStationCache> {

    @Override
    public void setSkuTaskInfo(ViewContext<OutboundWorkStationCache> viewContext) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.OUTBOUND_SKU_AREA;
    }
}
