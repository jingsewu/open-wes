package org.openwes.station.controller.view.handler.impl.outbound;

import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.handler.PutWallAreaHandler;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.wes.api.basic.constants.PageFieldEnum;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OutboundPutWallAreaHandler extends PutWallAreaHandler<OutboundWorkStationCache> {

    @Override
    public void setActivePutWallAndDispatchedPutWallSlots(ViewContext<OutboundWorkStationCache> viewContext) {
        WorkStationConfigDTO workStationConfig = viewContext.getWorkStationDTO().getWorkStationConfig();

        List<OperationTaskVO> processingOperationTasks = viewContext.getWorkStationCache().getProcessingOperationTasks();
        if (CollectionUtils.isEmpty(processingOperationTasks)) {
            return;
        }

        OperationTaskVO firstVO = processingOperationTasks.stream().iterator().next();
        Map<String, List<OperationTaskVO>> dispatchedPutWallSlotTaskMap = processingOperationTasks.stream()
                .filter(v -> v.getOperationTaskDTO().getSourceContainerSlot().equals(firstVO.getOperationTaskDTO().getSourceContainerSlot()))
                .collect(Collectors.groupingBy(operationTaskVO -> operationTaskVO.getOperationTaskDTO().getTargetLocationCode()));

        String activePutWallCode = getActivePutWallCode(dispatchedPutWallSlotTaskMap.keySet(), viewContext);

        viewContext.getWorkStationVO().getPutWallArea().getPutWallViews()
                .forEach(putWallDTO -> {

                    putWallDTO.setActive(StringUtils.equals(activePutWallCode, putWallDTO.getPutWallCode()));

                    putWallDTO.getPutWallSlots().forEach(putWallSlot -> {
                        if (dispatchedPutWallSlotTaskMap.containsKey(putWallSlot.getPutWallSlotCode())
                                && PutWallSlotStatusEnum.BOUND == putWallSlot.getPutWallSlotStatus()) {

                            List<OperationTaskVO> tasks = dispatchedPutWallSlotTaskMap.get(putWallSlot.getPutWallSlotCode());
                            List<WorkStationConfigDTO.PageFieldConfig> putWallSlotDesc = buildPutWallSlotDesc(workStationConfig, tasks);

                            putWallSlot.setQtyDispatched(tasks.stream().mapToInt(vo -> vo.getOperationTaskDTO().getRequiredQty()).sum());
                            putWallSlot.setPutWallSlotStatus(PutWallSlotStatusEnum.DISPATCH);
                            putWallSlot.setPutWallSlotDesc(putWallSlotDesc);
                        }
                    });
                });

    }

    private String getActivePutWallCode(Set<String> dispatchedPutWallSlots, ViewContext<OutboundWorkStationCache> viewContext) {

        if (StringUtils.isNotEmpty(viewContext.getWorkStationCache().getActivePutWallCode())) {
            return viewContext.getWorkStationCache().getActivePutWallCode();
        }

        return viewContext.getWorkStationDTO().getPutWalls().stream()
                .filter(putWallDTO ->
                        putWallDTO.getPutWallSlots().stream().anyMatch(v -> dispatchedPutWallSlots.contains(v.getPutWallSlotCode())))
                .map(PutWallDTO::getPutWallCode).findFirst().orElse(null);
    }

    private List<WorkStationConfigDTO.PageFieldConfig> buildPutWallSlotDesc(WorkStationConfigDTO workStationConfig, List<OperationTaskVO> tasks) {
        if (workStationConfig.getPickingStationConfig() == null
                || CollectionUtils.isEmpty(workStationConfig.getPickingStationConfig().getPutWallSlotFields())) {
            return Collections.emptyList();
        }

        List<WorkStationConfigDTO.PageFieldConfig> putWallSlotFields = workStationConfig.getPickingStationConfig().getPutWallSlotFields();
        return putWallSlotFields.stream()
                .filter(WorkStationConfigDTO.PageFieldConfig::isDisplay)
                .map(field -> toPutWallSlotDesc(field, tasks))
                .toList();
    }

    private WorkStationConfigDTO.PageFieldConfig toPutWallSlotDesc(WorkStationConfigDTO.PageFieldConfig pageFieldConfig, List<OperationTaskVO> tasks) {
        WorkStationConfigDTO.PageFieldConfig putWallSlotField = new WorkStationConfigDTO.PageFieldConfig();
        BeanUtils.copyProperties(pageFieldConfig, putWallSlotField);

        if (PageFieldEnum.MAX_PRIORITY.getFieldName().equals(putWallSlotField.getFieldName())) {
            Integer maxPriority = tasks.stream()
                    .max(Comparator.comparing(a -> a.getOperationTaskDTO().getPriority()))
                    .map(vo -> vo.getOperationTaskDTO().getPriority()).orElse(0);
            putWallSlotField.setFieldValue(String.valueOf(maxPriority));
        } else if (PageFieldEnum.REQUIRED_QTY.getFieldName().equals(putWallSlotField.getFieldName())) {
            int requiredQty = tasks.stream().mapToInt(vo -> vo.getOperationTaskDTO().getRequiredQty()).sum();
            putWallSlotField.setFieldValue(String.valueOf(requiredQty));
        } else if (PageFieldEnum.TO_BE_OPERATED_QTY.getFieldName().equals(putWallSlotField.getFieldName())) {
            int tobeOperatedQty = tasks.stream().mapToInt(vo -> vo.getOperationTaskDTO().getToBeOperatedQty()).sum();
            putWallSlotField.setFieldValue(String.valueOf(tobeOperatedQty));
        } else if (PageFieldEnum.OPERATED_QTY.getFieldName().equals(putWallSlotField.getFieldName())) {
            int operatedQty = tasks.stream().mapToInt(vo -> vo.getOperationTaskDTO().getOperatedQty()).sum();
            putWallSlotField.setFieldValue(String.valueOf(operatedQty));
        }
        return putWallSlotField;
    }
}
