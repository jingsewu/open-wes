package org.openwes.station.controller.view.handler;

import org.openwes.station.api.model.Tip;
import org.openwes.common.utils.exception.code_enum.IBaseError;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.WorkStationCache;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stubbed out — will be deleted in Phase 5 (view layer removal).
 */
@Service
@RequiredArgsConstructor
public class TipsHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        WorkStationCache workStationCache = viewContext.getWorkStationCache();

        List<Tip> tips = workStationCache.getTips();
        if (CollectionUtils.isEmpty(tips)) {
            return;
        }

        for (Tip tip : tips) {
            if (Tip.TipShowTypeEnum.TIP.getValue().equals(tip.getType()) && tip.getData() instanceof IBaseError error) {
                tip.setData(error.getDesc());
            }
        }

        // Note: type mismatch between model.Tip and WorkStationVO.Tip will be resolved when view layer is deleted
        // viewContext.getWorkStationVO().setTips(tips);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.TIPS;
    }
}
