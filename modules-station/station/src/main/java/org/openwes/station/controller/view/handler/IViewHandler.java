package org.openwes.station.controller.view.handler;

import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;

public interface IViewHandler<T extends WorkStationCache> {

    void buildView(ViewContext<T> viewContext);

    ViewHandlerTypeEnum getViewTypeEnum();

}
