package org.openwes.station.controller.view.handler.impl.outbound;

import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.BaseAreaHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OutboundBaseAreaHandler extends BaseAreaHandler<OutboundWorkStationCache> {

    @Override
    protected void setChooseArea(ViewContext<OutboundWorkStationCache> viewContext) {
        OutboundWorkStationCache workStationCache = viewContext.getWorkStationCache();
        WorkStationVO.ChooseAreaEnum chooseArea = workStationCache.getChooseArea();
        if (chooseArea != null) {
            viewContext.getWorkStationVO().setChooseArea(chooseArea);
            return;
        }

        List<OperationTaskVO> operateTasks = workStationCache.getOperateTasks();
        if (CollectionUtils.isEmpty(operateTasks)) {
            viewContext.getWorkStationVO().setChooseArea(WorkStationVO.ChooseAreaEnum.PUT_WALL_AREA);
            return;
        }

        WorkStationDTO workStationDTO = viewContext.getWorkStationDTO();
        Set<String> targetPutWallSlotCodes = operateTasks.stream()
            .map(vo -> vo.getOperationTaskDTO().getTargetLocationCode()).collect(Collectors.toSet());
        List<PutWallDTO> putWalls = workStationDTO.getPutWalls();
        List<PutWallSlotDTO> waitingBingingSlots = putWalls.stream().flatMap(putWall -> putWall.getPutWallSlots().stream())
            .filter(PutWallSlotDTO::isEnable)
            .filter(slot -> targetPutWallSlotCodes.contains(slot.getPutWallSlotCode()))
            .filter(slot -> PutWallSlotStatusEnum.WAITING_BINDING == slot.getPutWallSlotStatus()).toList();

        // has unbound container slot, or has not been scan sku
        if (CollectionUtils.isNotEmpty(waitingBingingSlots)) {
            viewContext.getWorkStationVO().setChooseArea(WorkStationVO.ChooseAreaEnum.PUT_WALL_AREA);
            return;
        }

        // scanned a sku
        if (CollectionUtils.isNotEmpty(workStationCache.getProcessingOperationTasks())) {
            viewContext.getWorkStationVO().setChooseArea(WorkStationVO.ChooseAreaEnum.PUT_WALL_AREA);
            return;
        }

        viewContext.getWorkStationVO().setChooseArea(WorkStationVO.ChooseAreaEnum.SKU_AREA);
    }


    @Override
    protected void setToolbar(ViewContext<OutboundWorkStationCache> viewContext) {
        WorkStationVO.Toolbar toolbar = new WorkStationVO.Toolbar();
        toolbar.setEnableReleaseSlot(true);
        toolbar.setEnableSplitContainer(true);

        OutboundWorkStationCache workStationCache = viewContext.getWorkStationCache();
        List<ArrivedContainerCache> arrivedContainers = workStationCache.getArrivedContainers();
        List<OperationTaskVO> processingOperationTasks = workStationCache.getProcessingOperationTasks();
        toolbar.setEnableReportAbnormal(CollectionUtils.isNotEmpty(arrivedContainers)
            && Optional.ofNullable(processingOperationTasks).stream()
            .flatMap(Collection::stream)
            .map(OperationTaskVO::getOperationTaskDTO)
            .map(Optional::ofNullable)
            .filter(Optional::isPresent)
            .noneMatch(task -> task.get().getAbnormalQty() > 0));

        viewContext.getWorkStationVO().setToolbar(toolbar);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.OUTBOUND_BASE_AREA;
    }

}
