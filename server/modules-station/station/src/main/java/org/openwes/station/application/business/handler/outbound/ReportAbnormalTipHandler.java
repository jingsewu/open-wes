package org.openwes.station.application.business.handler.outbound;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OutboundErrorDescEnum;
import org.openwes.common.utils.id.Snowflake;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.api.model.SkuArea;
import org.openwes.station.api.model.Tip;
import org.openwes.station.api.constants.ChooseAreaEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.model.ReportAbnormalTipData;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * TODO by Kinser
 *  Refactor the whole logic of report abnormal tip
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAbnormalTipHandler implements IBusinessHandler<Integer> {

    private final WorkStationService workStationService;
    private final WorkStationCacheRepository workStationRepository;
    private final Snowflake snowflake;
    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public void execute(Integer body, Long workStationId) {
        OutboundWorkStationCache workStationCache = (OutboundWorkStationCache) workStationService.getOrThrow(workStationId);

        List<ArrivedContainerCache> arrivedContainers = workStationCache.getWorkLocationArea().getAllContainers();
        if (CollectionUtils.isEmpty(arrivedContainers)) {
            throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_ARRIVED_CONTAINER);
        }

        List<OperationTaskDTO> processingTasks = workStationCache.getProcessingOperationTasks();
        if (CollectionUtils.isEmpty(processingTasks)) {
            throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_SCANNED_SKU);
        }

        // avoid unbound slot picking order short complete after report abnormal
        Set<String> processingSlotCodes = processingTasks.stream()
                .map(OperationTaskDTO::getTargetLocationCode).collect(Collectors.toSet());
        List<String> boundPutWallSlotCodes = remoteWorkStationService.queryPutWallSlots(workStationId, processingSlotCodes)
                .stream().filter(PutWallSlotDTO::isEnable).filter(v -> PutWallSlotStatusEnum.BOUND == v.getPutWallSlotStatus())
                .map(PutWallSlotDTO::getPutWallSlotCode).toList();

        List<OperationTaskDTO> tempAllTasks = new ArrayList<>(processingTasks.size());

        if (CollectionUtils.isNotEmpty(processingTasks)) {
            // calculate operated qty order by required qty descending
            List<OperationTaskDTO> sortedAllTasks = processingTasks.stream()
                    .filter(v -> boundPutWallSlotCodes.contains(v.getTargetLocationCode()))
                    .sorted(Comparator.comparing(OperationTaskDTO::getRequiredQty)).toList();

            final AtomicInteger totalToOperatedQty = new AtomicInteger(body);
            for (OperationTaskDTO taskDTO : sortedAllTasks) {
                int toBeOperatedQty = Math.min(taskDTO.getRequiredQty(), totalToOperatedQty.get());

                OperationTaskDTO task = new OperationTaskDTO();
                BeanUtils.copyProperties(taskDTO, task);
                tempAllTasks.add(task);

                task.setAbnormalQty(taskDTO.getRequiredQty() - toBeOperatedQty);

                // be careful implicit computation in getToBeOperatedQty
                totalToOperatedQty.set(Math.max(0, totalToOperatedQty.get() - taskDTO.getToBeOperatedQty()));
            }

            if (totalToOperatedQty.get() > 0) {
                log.warn("work station: {} code: {} occur overflow abnormal pick, overflow qty: {}",workStationId, workStationCache.getStationCode(), totalToOperatedQty);
            }
        }

        // clear tips
        workStationCache.closeTip(null);

        ReportAbnormalTipData tipData = buildTipData(arrivedContainers.get(0), workStationCache, processingTasks, tempAllTasks);

        Tip tip = new Tip();
        tip.setTipType(Tip.TipTypeEnum.REPORT_ABNORMAL_TIP);
        tip.setType(Tip.TipShowTypeEnum.CONFIRM.getValue());
        tip.setData(tipData);
        tip.setTipCode(String.valueOf(snowflake.nextId()));

        workStationCache.addTip(tip);
        workStationCache.setChooseArea(ChooseAreaEnum.TIPS);
        workStationRepository.save(workStationCache);
    }

    private ReportAbnormalTipData buildTipData(ArrivedContainerCache arrivedContainer,
                                               OutboundWorkStationCache workStationCache,
                                               List<OperationTaskDTO> allTasks, List<OperationTaskDTO> tempAllTasks) {
        int totalToBeRequiredQty = allTasks.stream().mapToInt(OperationTaskDTO::getToBeOperatedQty).sum();

        // distinct by sku code from SkuArea.SkuTaskInfo
        Map<String, SkuMainDataDTO> skuMainDataDTOMap = new LinkedHashMap<>();
        if (workStationCache.getSkuArea() != null && workStationCache.getSkuArea().getOperationViews() != null) {
            for (SkuArea.SkuTaskInfo info : workStationCache.getSkuArea().getOperationViews()) {
                if (info.getSkuMainDataDTO() != null) {
                    skuMainDataDTOMap.putIfAbsent(info.getSkuMainDataDTO().getSkuCode(), info.getSkuMainDataDTO());
                }
            }
        }

        ReportAbnormalTipData tipData = new ReportAbnormalTipData();
        tipData.setTotalToBeRequiredQty(totalToBeRequiredQty);
        tipData.setArrivedContainer(arrivedContainer);
        tipData.setSkuMainDataDTOS(skuMainDataDTOMap.values());
        tipData.setOperationTaskDTOS(tempAllTasks);

        return tipData;
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.REPORT_ABNORMAL_TIP;
    }

    @Override
    public Class<Integer> getParameterClass() {
        return Integer.class;
    }
}
