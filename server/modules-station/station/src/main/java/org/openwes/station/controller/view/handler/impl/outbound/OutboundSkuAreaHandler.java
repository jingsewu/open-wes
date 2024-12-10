package org.openwes.station.controller.view.handler.impl.outbound;

import com.google.common.collect.Lists;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.handler.SkuAreaHandler;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboundSkuAreaHandler extends SkuAreaHandler<OutboundWorkStationCache> {

    /**
     * push first sku batch picking task to view
     *
     * @param viewContext
     */
    @Override
    public void setSkuTaskInfo(ViewContext<OutboundWorkStationCache> viewContext) {

        OutboundWorkStationCache workStationCache = viewContext.getWorkStationCache();

        if (CollectionUtils.isEmpty(workStationCache.getOperateTasks())) {
            return;
        }

        WorkStationVO workStationVO = viewContext.getWorkStationVO();

        OperationTaskVO firstTaskVO = workStationCache.getFirstOperationTaskVO();
        OperationTaskDTO operationTaskDTO = firstTaskVO.getOperationTaskDTO();

        List<OperationTaskDTO> operationTaskDTOs = workStationCache.getOperateTasks().stream()
            .map(OperationTaskVO::getOperationTaskDTO)
            .filter(vo ->
                operationTaskDTO.getSourceContainerCode().equals(vo.getSourceContainerCode()) &&
                    operationTaskDTO.getSourceContainerSlot().equals(vo.getSourceContainerSlot()) &&
                    operationTaskDTO.getSkuBatchStockId().equals(vo.getSkuBatchStockId())).toList();

        WorkStationVO.SkuArea.SkuTaskInfo skuTaskInfo = new WorkStationVO.SkuArea
            .SkuTaskInfo(firstTaskVO.getSkuMainDataDTO(), firstTaskVO.getSkuBatchAttributeDTO(), operationTaskDTOs);

        workStationVO.getSkuArea().setPickingViews(Lists.newArrayList(skuTaskInfo));
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.OUTBOUND_SKU_AREA;
    }

}
