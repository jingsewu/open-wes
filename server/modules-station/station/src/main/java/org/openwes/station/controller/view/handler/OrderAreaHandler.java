package org.openwes.station.controller.view.handler;

import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.springframework.stereotype.Service;

@Service
public class OrderAreaHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        final WorkStationVO workStationVO = viewContext.getWorkStationVO();
        if (workStationVO == null) {
            return;
        }

        buildOrderArea(viewContext);
    }

    public void buildOrderArea(ViewContext<T> viewContext) {

    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.ORDER_AREA;
    }

}
