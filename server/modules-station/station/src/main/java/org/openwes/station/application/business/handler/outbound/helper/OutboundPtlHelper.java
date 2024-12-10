package org.openwes.station.application.business.handler.outbound.helper;

import org.openwes.station.api.IPtlApi;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboundPtlHelper {

    private final IPtlApi ptlApi;

    public void send(ApiCodeEnum apiCode, OutboundWorkStationCache workStationCache) {

        Map<String, Integer> putWallSlotToBePickingQtyMap = workStationCache.getProcessingOperationTasks().stream().map(OperationTaskVO::getOperationTaskDTO)
            .collect(Collectors.groupingBy(OperationTaskDTO::getTargetLocationCode, Collectors.summingInt(OperationTaskDTO::getToBeOperatedQty)));

        switch (apiCode) {
            case REPORT_ABNORMAL:
                putWallSlotToBePickingQtyMap.forEach((putWallSlotCode, qty) -> {
                    if (qty > 0) {
                        workStationCache.getPutWallSlot(putWallSlotCode).ifPresent(putWallSlot ->
                            ptlApi.reminderDispatch(workStationCache.getId(), putWallSlot.getPtlTag(), qty, ""));
                    } else if (qty == 0) {
                        workStationCache.getPutWallSlot(putWallSlotCode).ifPresent(putWallSlot ->
                            ptlApi.off(workStationCache.getId(), putWallSlot.getPtlTag()));
                    }
                });
                break;

            case SCAN_BARCODE:
                putWallSlotToBePickingQtyMap.forEach((putWallSlotCode, qty) ->
                    workStationCache.getPutWallSlot(putWallSlotCode).ifPresent(putWallSlot ->
                        ptlApi.reminderDispatch(workStationCache.getId(), putWallSlot.getPtlTag(), qty, "")));
                break;

            case INPUT:
                workStationCache.getPutWallSlot(workStationCache.getInputPutWallSlot())
                    .ifPresent(v -> ptlApi.off(workStationCache.getId(), v.getPtlTag()));

                //由于绑定料箱的前一个操作是拆箱动作或者已经扫过SKU，所以这里绑定料箱后，需要重新让电子标签亮起来
                putWallSlotToBePickingQtyMap.forEach((putWallSlotCode, qty) -> {
                    if (StringUtils.equals(putWallSlotCode, workStationCache.getInputPutWallSlot())) {
                        workStationCache.getPutWallSlot(putWallSlotCode).ifPresent(putWallSlot ->
                            ptlApi.reminderDispatch(workStationCache.getId(), putWallSlot.getPtlTag(), qty, ""));
                    }
                });
                break;

            default:
                break;
        }
    }

}
