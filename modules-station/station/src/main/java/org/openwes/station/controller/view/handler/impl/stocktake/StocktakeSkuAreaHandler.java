package org.openwes.station.controller.view.handler.impl.stocktake;

import com.google.common.collect.Lists;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.SkuAreaHandler;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StocktakeSkuAreaHandler extends SkuAreaHandler<StocktakeWorkStationCache> {

    @Override
    protected void setSkuTaskInfo(ViewContext<StocktakeWorkStationCache> viewContext) {
        StocktakeWorkStationCache workStationCache = viewContext.getWorkStationCache();

        if (CollectionUtils.isEmpty(workStationCache.getOperateTasks())) {
            return;
        }

        WorkStationVO workStationVO = viewContext.getWorkStationVO();

        OperationTaskVO operationTaskVO = workStationCache.getFirstOperationTaskVO();

        if (operationTaskVO != null) {
            OperationTaskDTO operationTaskDTO = operationTaskVO.getOperationTaskDTO();

            WorkStationVO.SkuArea.SkuTaskInfo skuTaskInfo = new WorkStationVO.SkuArea.SkuTaskInfo(
                    operationTaskVO.getSkuMainDataDTO(),
                    operationTaskVO.getSkuBatchAttributeDTO(),
                    Lists.newArrayList(operationTaskDTO)
            );
            workStationVO.getSkuArea().setPickingViews(Lists.newArrayList(skuTaskInfo));
        }
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.STOCKTAKE_SKU_AREA;
    }
}
