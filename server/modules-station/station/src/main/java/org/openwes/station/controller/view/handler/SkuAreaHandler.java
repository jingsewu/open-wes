package org.openwes.station.controller.view.handler;

import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.WorkStationCache;
import org.springframework.stereotype.Service;

@Service
public class SkuAreaHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        setSkuTaskInfo(viewContext);
    }

    protected void setSkuTaskInfo(ViewContext<T> viewContext) {
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.SKU_AREA;
    }
}
