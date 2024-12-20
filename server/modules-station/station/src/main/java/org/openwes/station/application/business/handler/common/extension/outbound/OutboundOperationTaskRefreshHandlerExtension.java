package org.openwes.station.application.business.handler.common.extension.outbound;


import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.ContainerService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboundOperationTaskRefreshHandlerExtension
        implements OperationTaskRefreshHandler.Extension<OutboundWorkStationCache> {

    private final TaskService taskService;
    private final EquipmentService equipmentService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;
    private final ContainerService containerService;

    @Override
    public void refresh(OutboundWorkStationCache workStationCache) {
        if (CollectionUtils.isNotEmpty(workStationCache.getOperateTasks())) {
            return;
        }

        workStationCache.clearOperateTasks();

        List<ArrivedContainerCache> doneContainers = workStationCache.queryTasksAndReturnRemovedContainers(taskService);
        workStationRepository.save(workStationCache);

        if (CollectionUtils.isEmpty(doneContainers)) {
            return;
        }
        WorkStationConfigDTO workStationConfig = workStationCache.getWorkStationConfig();
        if (workStationConfig == null || !workStationConfig.getPickingStationConfig().isEmptyToteRecycle()) {
            equipmentService.containerLeave(doneContainers, ContainerOperationTypeEnum.LEAVE);
            return;
        }

        ContainerDTO containerDTO = containerService.queryContainer(doneContainers.get(0).getContainerCode(),
                workStationCache.getWarehouseCode());

        if (containerDTO.isEmptyContainer()) {
            addTip(workStationCache, containerDTO.getContainerCode());
        }
    }

    private void addTip(OutboundWorkStationCache workStationCache, String containerCode) {

        WorkStationVO.Tip tip = new WorkStationVO.Tip();
        tip.setTipType(WorkStationVO.Tip.TipTypeEnum.EMPTY_CONTAINER_HANDLE_TIP);
        tip.setType(WorkStationVO.Tip.TipShowTypeEnum.CONFIRM.getValue());
        tip.setTipCode(UUID.randomUUID().toString());
        tip.setData(containerCode);

        workStationCache.addTip(tip);
        workStationCache.setChooseArea(WorkStationVO.ChooseAreaEnum.TIPS);
        workStationRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.PICKING;
    }

}
