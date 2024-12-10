package org.openwes.station.application.business.handler.outbound;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OutboundErrorDescEnum;
import org.openwes.common.utils.id.Snowflake;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.model.ReportAbnormalTipData;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
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

    private final WorkStationService<OutboundWorkStationCache> workStationService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;
    private final Snowflake snowflake;
    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public void execute(Integer body, Long workStationId) {
        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);

        List<ArrivedContainerCache> arrivedContainers = workStationCache.getArrivedContainers();
        if (CollectionUtils.isEmpty(arrivedContainers)) {
            throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_ARRIVED_CONTAINER);
        }

        List<OperationTaskVO> allTaskVOs = workStationCache.getProcessingOperationTasks();
        if (CollectionUtils.isEmpty(allTaskVOs)) {
            throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_SCANNED_SKU);
        }

        // avoid unbound slot picking order short complete after report abnormal
        Set<String> processingSlotCodes = allTaskVOs.stream()
                .map(v -> v.getOperationTaskDTO().getTargetLocationCode()).collect(Collectors.toSet());
        List<String> boundPutWallSlotCodes = remoteWorkStationService.queryPutWallSlots(workStationId, processingSlotCodes)
                .stream().filter(PutWallSlotDTO::isEnable).filter(v -> PutWallSlotStatusEnum.BOUND == v.getPutWallSlotStatus())
                .map(PutWallSlotDTO::getPutWallSlotCode).toList();

        List<OperationTaskDTO> tempAllTasks = new ArrayList<>(allTaskVOs.size());

        if (CollectionUtils.isNotEmpty(allTaskVOs)) {
            // calculate operated qty order by required qty descending
            List<OperationTaskVO> sortedAllTasks = allTaskVOs.stream()
                    .filter(v -> boundPutWallSlotCodes.contains(v.getOperationTaskDTO().getTargetLocationCode()))
                    .sorted(Comparator.comparing(a -> a.getOperationTaskDTO().getRequiredQty())).toList();

            final AtomicInteger totalToOperatedQty = new AtomicInteger(body);
            for (OperationTaskVO taskVO : sortedAllTasks) {
                int toBeOperatedQty = Math.min(taskVO.getOperationTaskDTO().getRequiredQty(), totalToOperatedQty.get());

                OperationTaskDTO task = new OperationTaskDTO();
                BeanUtils.copyProperties(taskVO.getOperationTaskDTO(), task);
                tempAllTasks.add(task);

                task.setAbnormalQty(taskVO.getOperationTaskDTO().getRequiredQty() - toBeOperatedQty);

                // be careful implicit computation in getToBeOperatedQty
                totalToOperatedQty.set(Math.max(0, totalToOperatedQty.get() - taskVO.getOperationTaskDTO().getToBeOperatedQty()));
            }

            if (totalToOperatedQty.get() > 0) {
                log.warn("station {} occur overflow abnormal pick, overflow qty {}", workStationCache.getStationCode(), totalToOperatedQty);
            }
        }

        // clear tips
        workStationCache.closeTip(null);

        ReportAbnormalTipData tipData = buildTipData(arrivedContainers.get(0), allTaskVOs, tempAllTasks);

        WorkStationVO.Tip tip = new WorkStationVO.Tip();
        tip.setTipType(WorkStationVO.Tip.TipTypeEnum.REPORT_ABNORMAL_TIP);
        tip.setType(WorkStationVO.Tip.TipShowTypeEnum.CONFIRM.getValue());
        tip.setData(tipData);
        tip.setTipCode(String.valueOf(snowflake.nextId()));

        workStationCache.addTip(tip);
        workStationCache.setChooseArea(WorkStationVO.ChooseAreaEnum.TIPS);
        workStationRepository.save(workStationCache);
    }

    private ReportAbnormalTipData buildTipData(ArrivedContainerCache arrivedContainer,
                                               List<OperationTaskVO> allTasks, List<OperationTaskDTO> tempAllTasks) {
        List<OperationTaskDTO> operationTaskDTOS = allTasks.stream().map(OperationTaskVO::getOperationTaskDTO).toList();
        int totalToBeRequiredQty = operationTaskDTOS.stream().mapToInt(OperationTaskDTO::getToBeOperatedQty).sum();

        // distinct by sku code
        Map<String, SkuMainDataDTO> skuMainDataDTOMap = allTasks.stream()
                .map(OperationTaskVO::getSkuMainDataDTO)
                .collect(Collectors.toMap(SkuMainDataDTO::getSkuCode, dto -> dto, (a, b) -> a));

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
