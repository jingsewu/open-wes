package org.openwes.station.controller.view.handler;

import com.google.common.collect.Sets;
import org.openwes.station.api.vo.WorkLocationExtend;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.transfer.ArriveContainerCacheTransfer;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContainerAreaHandler<T extends WorkStationCache> implements IViewHandler<T> {

    protected final ArriveContainerCacheTransfer arriveContainerCacheTransfer;

    @Override
    public void buildView(ViewContext<T> viewContext) {
        WorkStationCache workStationCache = viewContext.getWorkStationCache();
        if (CollectionUtils.isEmpty(workStationCache.getArrivedContainers())) {
            return;
        }

        setArrivedContainersOnLocation(viewContext);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.CONTAINER_AREA;
    }

    private void setArrivedContainersOnLocation(ViewContext<T> viewContext) {
        WorkStationVO workStationVO = viewContext.getWorkStationVO();

        List<WorkLocationExtend> workLocationViews = workStationVO.getWorkLocationArea().getWorkLocationViews();
        if (CollectionUtils.isEmpty(workLocationViews)) {
            return;
        }

        WorkStationCache workStationCache = viewContext.getWorkStationCache();
        List<ArrivedContainerCache> arrivedContainers = workStationCache.getArrivedContainers();

        if (ObjectUtils.isEmpty(arrivedContainers)) {
            return;
        }

        setActiveContainerSlots(workStationCache, arrivedContainers);

        setContainersOnLocation(arrivedContainers, workLocationViews);
    }

    private void setContainersOnLocation(List<ArrivedContainerCache> arrivedContainers, List<WorkLocationExtend> workLocationViews) {
        Map<String, List<ArrivedContainerCache>> locationContainerMap = arrivedContainers.stream().collect(Collectors.groupingBy(ArrivedContainerCache::getWorkLocationCode));

        for (WorkLocationExtend workLocation : workLocationViews) {
            List<ArrivedContainerCache> locationContainers = locationContainerMap.get(workLocation.getWorkLocationCode());
            if (CollectionUtils.isEmpty(locationContainers)) {
                continue;
            }

            if (CollectionUtils.isEmpty(workLocation.getWorkLocationSlots())) {
                List<WorkLocationExtend.WorkLocationSlotExtend> workLocationSlots = locationContainers.stream()
                        .map(v -> getWorkLocationSlotExtend(workLocation, v))
                        .toList();
                workLocation.setWorkLocationSlots(workLocationSlots);
            } else {
                workLocation.getWorkLocationSlots().forEach(workLocationSlotExtend ->
                        locationContainers.forEach(arrivedContainer -> {
                            if (workLocationSlotExtend.getSlotCode().equals(arrivedContainer.getLocationCode())) {
                                workLocationSlotExtend.setArrivedContainer(arriveContainerCacheTransfer.toDTO(arrivedContainer));
                            }
                        }));
            }
        }
    }

    private void setActiveContainerSlots(WorkStationCache workStationCache, List<ArrivedContainerCache> arrivedContainers) {
        OperationTaskVO firstOperationTask = workStationCache.getFirstOperationTaskVO();
        if (firstOperationTask != null && firstOperationTask.getOperationTaskDTO() != null) {
            OperationTaskDTO operationTaskDTO = firstOperationTask.getOperationTaskDTO();
            arrivedContainers.stream()
                    .filter(c -> c.getContainerCode().equals(operationTaskDTO.getSourceContainerCode())
                            && c.getFace().equals(operationTaskDTO.getSourceContainerFace()))
                    .findFirst()
                    .ifPresent(c -> c.setActiveSlotCodes(Sets.newHashSet(operationTaskDTO.getSourceContainerSlot())));
        }
    }

    protected WorkLocationExtend.WorkLocationSlotExtend getWorkLocationSlotExtend(WorkLocationExtend workLocationView, ArrivedContainerCache container) {
        WorkLocationExtend.WorkLocationSlotExtend workLocationSlotExtend = new WorkLocationExtend.WorkLocationSlotExtend();
        workLocationSlotExtend.setArrivedContainer(arriveContainerCacheTransfer.toDTO(container));
        workLocationSlotExtend.setBay(container.getBay());
        workLocationSlotExtend.setLevel(container.getLevel());
        workLocationSlotExtend.setEnable(true);
        workLocationSlotExtend.setSlotCode(container.getLocationCode());
        workLocationSlotExtend.setGroupCode(container.getGroupCode());
        workLocationSlotExtend.setWorkLocationCode(workLocationView.getWorkLocationCode());
        return workLocationSlotExtend;
    }

}
