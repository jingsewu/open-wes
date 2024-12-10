package org.openwes.station.controller.view.handler;

import org.openwes.common.utils.exception.code_enum.IBaseError;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.WorkStationCache;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipsHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        WorkStationCache workStationCache = viewContext.getWorkStationCache();

        List<WorkStationVO.Tip> tips = workStationCache.getTips();
        if (CollectionUtils.isEmpty(tips)) {
            return;
        }

        for (WorkStationVO.Tip tip : tips) {
            if (WorkStationVO.Tip.TipShowTypeEnum.TIP.getValue().equals(tip.getType()) && tip.getData() instanceof IBaseError error) {
                tip.setData(error.getDesc());
            }
        }

        viewContext.getWorkStationVO().setTips(tips);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.TIPS;
    }
}
