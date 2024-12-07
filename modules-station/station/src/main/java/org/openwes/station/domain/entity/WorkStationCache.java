package org.openwes.station.domain.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskVO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum.INCORRECT_BARCODE;

/**
 * definition：a place that operators working, only support one station one Operation Type at a time.
 * <p>
 * a base work station Entity , only contains the basic information of work station. if you need to add more information,
 * please add it to the subclasses. such as InboundWorkStationCache and OutboundWorkStationCache.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WorkStationCache {

    protected Long id;

    protected String warehouseCode;
    protected Long warehouseAreaId;
    protected String stationCode;

    protected WorkStationModeEnum workStationMode;

    protected List<OperationTaskVO> operateTasks;

    protected List<ArrivedContainerCache> arrivedContainers;

    //just use it as the cache of putWallSlots. its status is not right.
    protected List<PutWallSlotDTO> putWallSlots;

    protected WorkStationConfigDTO workStationConfig;
    protected WorkStationVO.ChooseAreaEnum chooseArea;
    protected List<WorkStationVO.Tip> tips;

    protected ApiCodeEnum eventCode;

    protected String scannedBarcode;

    public void online(WorkStationModeEnum workStationMode) {
        this.workStationMode = workStationMode;
    }

    public WorkStationConfigDTO getWorkStationConfig() {
        return workStationConfig == null ? new WorkStationConfigDTO() : workStationConfig;
    }

    /**
     * set arrived containers on the location
     *
     * @param newArrivedContainers the arrived containers
     */
    public void addArrivedContainers(List<ArrivedContainerCache> newArrivedContainers) {

        log.info("work station: {} add arrived containers: {}", this.id, newArrivedContainers);

        if (CollectionUtils.isEmpty(this.arrivedContainers)) {
            this.arrivedContainers = Lists.newArrayList(newArrivedContainers);
        } else {
            this.arrivedContainers.addAll(newArrivedContainers);
        }
    }

    public void setUndoContainersProcessing(List<ArrivedContainerCache> arrivedContainers) {

        log.info("work station: {} set undo containers: {} processing", this.id, arrivedContainers);

        if (CollectionUtils.isEmpty(arrivedContainers)) {
            return;
        }

        if (this.workStationMode == WorkStationModeEnum.ONE_STEP_RELOCATION) {
            arrivedContainers = arrivedContainers.subList(0, 2);
        } else {
            arrivedContainers = arrivedContainers.subList(0, 1);
        }
        arrivedContainers.forEach(v -> v.setProcessStatus(ProcessStatusEnum.PROCESSING));
    }

    public Collection<ArrivedContainerCache> removeProceedContainers() {
        Set<String> groupCodes = Sets.newHashSet();
        this.arrivedContainers.stream().collect(Collectors.groupingBy(ArrivedContainerCache::getGroupCode))
                .forEach((groupCode, containers) -> {
                    if (containers.stream().allMatch(v -> v.getProcessStatus() == ProcessStatusEnum.PROCEED)) {
                        groupCodes.add(groupCode);
                    }
                });

        List<ArrivedContainerCache> doneContainers = this.arrivedContainers.stream().filter(v -> groupCodes.contains(v.getGroupCode())).toList();
        this.arrivedContainers.removeIf(v -> groupCodes.contains(v.getGroupCode()));

        log.info("work station: {} remove proceed container:  {}", this.id, doneContainers);

        return doneContainers;
    }

    public List<ArrivedContainerCache> getUndoContainers() {
        return arrivedContainers.stream()
                .filter(v -> v.getProcessStatus() == ProcessStatusEnum.PROCESSING || v.getProcessStatus() == ProcessStatusEnum.UNDO).toList();
    }

    public void updateConfiguration(WorkStationConfigDTO workStationConfigDTO) {
        log.info("work station: {} update configuration: {}", this.id, workStationConfigDTO);
        this.workStationConfig = workStationConfigDTO;
    }

    public void addTip(WorkStationVO.Tip tip) {
        log.info("work station: {} add tip: {}", this.id, tip);
        if (this.tips == null) {
            this.tips = Lists.newArrayList();
        }
        // avoid repeat confirm tip
        this.tips.removeIf(exitsTip
                -> WorkStationVO.Tip.TipShowTypeEnum.CONFIRM.getValue().equals(exitsTip.getType()));

        tips.add(tip);
    }

    public void closeTip(String tipCode) {
        log.info("work station: {} close tip: {}", this.id, tipCode);
        if (this.tips == null) {
            return;
        }
        if (tipCode == null) {
            this.tips.clear();
        }
        this.tips.removeIf(tip -> tip.getTipCode().equals(tipCode));
    }

    public Optional<PutWallSlotDTO> getPutWallSlot(String putWallSlotCode) {
        return this.putWallSlots.stream()
                .filter(putWallSlot -> StringUtils.equals(putWallSlot.getPutWallSlotCode(), putWallSlotCode))
                .findFirst();
    }

    public void scanBarcode(String barcode) {
        log.info("work station: {} scan barcode: {}", this.id, barcode);
        this.scannedBarcode = barcode;
    }

    public void clearOperateTasks() {
        log.info("work station: {} clear all operate tasks", this.id);
        if (CollectionUtils.isNotEmpty(this.operateTasks)) {
            this.operateTasks.clear();
        }
    }

    public void addOperateTasks(List<OperationTaskVO> containerOperateTasks) {

        log.info("work station: {} add operate tasks: {}", this.id, containerOperateTasks);

        if (CollectionUtils.isEmpty(containerOperateTasks)) {
            return;
        }

        if (this.operateTasks == null) {
            this.operateTasks = Lists.newArrayList(containerOperateTasks);
        } else {
            this.operateTasks.addAll(containerOperateTasks);
        }
    }

    public void processTasks(String skuCode) {

        log.info("work station: {} process sku: {} tasks", this.id, skuCode);

        this.chooseArea = null;
        this.scannedBarcode = skuCode;

        if (CollectionUtils.isEmpty(this.operateTasks)) {
            return;
        }

        OperationTaskVO firstTaskVO = this.operateTasks.stream()
                .filter(vo -> skuCode.equals(vo.getSkuMainDataDTO().getSkuCode())).findFirst().orElse(null);

        if (firstTaskVO == null) {
            throw WmsException.throwWmsException(INCORRECT_BARCODE);
        }

        for (OperationTaskVO operateTask : this.operateTasks) {
            // reset process status to avoid operator scan a barcode but not picking then
            // scan another barcode. ensure only one sku operation task be processing once.
            operateTask.getOperationTaskDTO().setTaskStatus(OperationTaskStatusEnum.NEW);
            if (firstTaskVO.getOperationTaskDTO().getSourceContainerSlot().equals(operateTask.getOperationTaskDTO().getSourceContainerSlot())
                    && firstTaskVO.getSkuBatchAttributeDTO().getId().equals(operateTask.getSkuBatchAttributeDTO().getId())) {
                operateTask.getOperationTaskDTO().setTaskStatus(OperationTaskStatusEnum.PROCESSING);
            }
        }

        resetActivePutWall(skuCode);
    }

    public OperationTaskVO getFirstOperationTaskVO() {
        if (CollectionUtils.isEmpty(this.operateTasks)) {
            return null;
        }
        return this.operateTasks.stream().iterator().next();
    }

    protected void resetActivePutWall(String skuCode) {
    }

    public OperationTaskVO getFirstProcessingTask() {
        if (this.operateTasks == null) {
            return null;
        }
        return this.operateTasks.stream().filter(v -> v.getOperationTaskDTO().getTaskStatus()
                == OperationTaskStatusEnum.PROCESSING).findFirst().orElse(null);
    }
}
