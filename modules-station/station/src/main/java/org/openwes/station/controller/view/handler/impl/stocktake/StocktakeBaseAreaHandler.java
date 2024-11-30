package org.openwes.station.controller.view.handler.impl.stocktake;

import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.BaseAreaHandler;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StocktakeBaseAreaHandler extends BaseAreaHandler<StocktakeWorkStationCache> {
    @Override
    protected void setChooseArea(ViewContext<StocktakeWorkStationCache> viewContext) {
        StocktakeWorkStationCache workStationCache = viewContext.getWorkStationCache();
        WorkStationVO workStationVO = viewContext.getWorkStationVO();
        workStationVO.setScanCode(workStationCache.getScannedBarcode());

        WorkStationVO.ChooseAreaEnum chooseArea = workStationCache.getChooseArea();
        if (chooseArea == null) {
            if (CollectionUtils.isNotEmpty(workStationCache.getOperateTasks())) {
                chooseArea = WorkStationVO.ChooseAreaEnum.SKU_AREA;
            }
        }

        if (chooseArea == null) {
            chooseArea = WorkStationVO.ChooseAreaEnum.CONTAINER_AREA;
        }
        workStationVO.setChooseArea(chooseArea);
    }

    @Override
    protected void setToolbar(ViewContext<StocktakeWorkStationCache> viewContext) {
        super.setToolbar(viewContext);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.STOCKTAKE_BASE_AREA;
    }
}
