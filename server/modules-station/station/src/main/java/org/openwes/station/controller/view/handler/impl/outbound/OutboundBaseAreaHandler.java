package org.openwes.station.controller.view.handler.impl.outbound;

import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.BaseAreaHandler;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.api.vo.WorkStationVO;
import org.springframework.stereotype.Service;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Service
public class OutboundBaseAreaHandler extends BaseAreaHandler<OutboundWorkStationCache> {

    @Override
    protected void setChooseArea(ViewContext<OutboundWorkStationCache> viewContext) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    protected void setToolbar(ViewContext<OutboundWorkStationCache> viewContext) {
        viewContext.getWorkStationVO().setToolbar(new WorkStationVO.Toolbar());
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.OUTBOUND_BASE_AREA;
    }
}
