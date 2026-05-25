package org.openwes.station.controller.view.handler.impl.stocktake;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.BaseAreaHandler;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.springframework.stereotype.Component;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StocktakeBaseAreaHandler extends BaseAreaHandler<StocktakeWorkStationCache> {

    private final StocktakeService stocktakeService;

    @Override
    protected void setChooseArea(ViewContext<StocktakeWorkStationCache> viewContext) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    protected void setOrderArea(ViewContext<StocktakeWorkStationCache> viewContext) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    public void setStationProcessingStatus(WorkStationVO workStationVO, StocktakeWorkStationCache workStationCache) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.STOCKTAKE_BASE_AREA;
    }
}
