package org.openwes.station.controller.view.handler;

import lombok.RequiredArgsConstructor;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.WorkStationCache;
import org.springframework.stereotype.Service;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Service
@RequiredArgsConstructor
public class ContainerAreaHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        // stubbed - will be removed in Phase 5
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.CONTAINER_AREA;
    }
}
