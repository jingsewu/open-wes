package org.openwes.station.application.business.handler.outbound;

import com.google.common.base.Preconditions;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.outbound.ReportAbnormalEvent;
import org.openwes.station.application.business.handler.outbound.helper.OutboundPtlHelper;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.openwes.wes.api.task.dto.ReportAbnormalDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAbnormalHandler implements IBusinessHandler<ReportAbnormalEvent> {

    private final WorkStationService<OutboundWorkStationCache> workStationService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;
    private final TaskService taskService;
    private final OutboundPtlHelper outboundPtlHelper;
    private final EquipmentService equipmentService;

    @Override
    public void execute(ReportAbnormalEvent handleTasksEvent, Long workStationId) {
        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);
        Preconditions.checkState(CollectionUtils.isNotEmpty(workStationCache.getOperateTasks()));

        Map<Long, ReportAbnormalEvent.ReportAbnormalTask> taskMap = handleTasksEvent.getReportAbnormalTasks()
                .stream().collect(Collectors.toMap(ReportAbnormalEvent.ReportAbnormalTask::getTaskId, task -> task));

        List<OperationTaskDTO> operateTasks = workStationCache.getOperateTasks().stream()
                .map(OperationTaskVO::getOperationTaskDTO)
                .filter(operationTaskDTO -> taskMap.containsKey(operationTaskDTO.getId()))
                // calculate operated qty order by required qty descending
                .sorted(Comparator.comparing(OperationTaskDTO::getOperatedQty)).toList();
        Preconditions.checkState(operateTasks.size() == taskMap.size());

        List<ReportAbnormalDTO.HandleTask> handleTasks = operateTasks.stream().map(operationTaskDTO -> {
            ReportAbnormalEvent.ReportAbnormalTask reportAbnormalTask = taskMap.get(operationTaskDTO.getId());
            return ReportAbnormalDTO.HandleTask.builder()
                    .taskId(operationTaskDTO.getId())
                    .abnormalQty(operationTaskDTO.getRequiredQty() - reportAbnormalTask.getToBeOperatedQty())
                    .requiredQty(operationTaskDTO.getRequiredQty())
                    .build();
        }).toList();

        ReportAbnormalDTO reportAbnormalDTO = ReportAbnormalDTO.builder()
                .abnormalReason(handleTasksEvent.getAbnormalReason())
                .handleTasks(handleTasks).build();
        taskService.reportAbnormal(reportAbnormalDTO);

        workStationCache.reportAbnormal(handleTasks);
        workStationCache.closeTip(null);
        workStationRepository.save(workStationCache);

        outboundPtlHelper.send(ApiCodeEnum.REPORT_ABNORMAL, workStationCache);

        doZeroPickingCompleted(workStationCache, handleTasksEvent);
    }

    private void doZeroPickingCompleted(OutboundWorkStationCache workStationCache, ReportAbnormalEvent reportAbnormalEvent) {
        if (reportAbnormalEvent.getReportAbnormalTasks().stream()
                .mapToInt(ReportAbnormalEvent.ReportAbnormalTask::getToBeOperatedQty).sum() == 0) {
            return;
        }
        Collection<ArrivedContainerCache> doneContainers = workStationCache.queryTasksAndReturnRemovedContainers(taskService);
        workStationRepository.save(workStationCache);

        if (CollectionUtils.isNotEmpty(doneContainers)) {
            equipmentService.containerLeave(doneContainers, ContainerOperationTypeEnum.LEAVE);
        }
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.REPORT_ABNORMAL;
    }

    @Override
    public Class<ReportAbnormalEvent> getParameterClass() {
        return ReportAbnormalEvent.class;
    }
}
