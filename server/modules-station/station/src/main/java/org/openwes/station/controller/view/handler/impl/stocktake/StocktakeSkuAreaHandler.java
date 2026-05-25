package org.openwes.station.controller.view.handler.impl.stocktake;

import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.SkuAreaHandler;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Slf4j
@Component
public class StocktakeSkuAreaHandler extends SkuAreaHandler<StocktakeWorkStationCache> {

    @Override
    protected void setSkuTaskInfo(ViewContext<StocktakeWorkStationCache> viewContext) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.STOCKTAKE_SKU_AREA;
    }
}
