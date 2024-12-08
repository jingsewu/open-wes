package org.openwes.wes.task.domain.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.basic.constants.WarehouseAreaTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.api.stock.event.StockTransferEvent;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;
import org.openwes.wes.api.task.event.PickingOrderPickingEvent;
import org.openwes.wes.common.constants.WmsCommonConstants;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum.REPEAT_REPORT_STOCK_ABNORMAL;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class OperationTask extends UpdateUserDTO {

    private Long id;

    private String warehouseCode;
    private OperationTaskTypeEnum taskType;

    private Long workStationId;

    private Integer priority;

    private Long skuId;
    private Long skuBatchStockId;
    private Long skuBatchAttributeId;
    private Long containerStockId;

    private String sourceContainerCode;
    private String sourceContainerFace;
    private String sourceContainerSlot;

    private String boxNo;

    private Integer requiredQty;
    private Integer operatedQty;
    //表示上报的异常数量
    private Integer abnormalQty;

    private String targetLocationCode;
    private String targetContainerCode;
    private String targetContainerSlotCode;
    private Long transferContainerRecordId;

    private Long orderId;
    //冗余订单号
    private String orderNo;
    private Long detailId;

    private Map<Long, String> assignedStationSlot;

    //当且仅当 this.abnormalQty + this.operatedQty == this.requiredQty 时,任务完成
    private OperationTaskStatusEnum taskStatus;

    private boolean abnormal;

    private Long version;


    public void validateQty() {
        Preconditions.checkState(this.requiredQty >= 0, "required qty must be greater or equal to 0, task id: %s", id);
        Preconditions.checkState(this.operatedQty >= 0, "operated qty must be greater or equal to 0, task id: %s", id);
        Preconditions.checkState(this.abnormalQty >= 0, "abnormal qty must be greater or equal to 0, task id: %s", id);
        Preconditions.checkState(this.requiredQty >= this.operatedQty + this.abnormalQty,
                "required qty must be greater or equal to operated qty + abnormal qty, task id: %s", id);
    }

    public void operate(Integer operatedQty, HandleTaskDTO.HandleTaskTypeEnum handleTaskType,
                        String transferContainerCode, Long transferContainerRecordId) {

        if (this.taskStatus == OperationTaskStatusEnum.PROCESSED || this.taskStatus == OperationTaskStatusEnum.CANCELED) {
            throw WmsException.throwWmsException(OperationTaskErrorDescEnum.OPERATION_TASK_IS_PROCESSED, this.id);
        }

        log.info("operation task: {} operatedQty: {} transferContainerCode: {} transferContainerRecordId: {} " +
                "with handleTaskType: {}", id, operatedQty, transferContainerCode, transferContainerRecordId, handleTaskType);


        if (StringUtils.isNotEmpty(transferContainerCode)) {
            this.targetContainerCode = transferContainerCode;
        }
        if (transferContainerRecordId != null) {
            this.transferContainerRecordId = transferContainerRecordId;
        }
        if (handleTaskType == HandleTaskDTO.HandleTaskTypeEnum.COMPLETE) {
            this.complete(operatedQty);
        } else {
            this.split(operatedQty);
        }
    }

    private void complete(Integer operatedQty) {

        log.info("operation task: {} operated qty: {}", this.id, operatedQty);

        this.operatedQty += operatedQty;
        validateQty();

        if (this.abnormalQty + this.operatedQty == this.requiredQty) {
            this.taskStatus = OperationTaskStatusEnum.PROCESSED;
        }

        sendStockTransferEvent(operatedQty);

        OperationTaskPickingDTO operationTaskPickingDTO = new OperationTaskPickingDTO().setOperatedQty(operatedQty)
                .setDetailId(this.detailId).setOrderId(this.orderId).setTaskType(this.taskType);
        DomainEventPublisher.sendAsyncDomainEvent(new PickingOrderPickingEvent().setOperationTasks(Lists.newArrayList(operationTaskPickingDTO)));
    }

    private void split(Integer operatedQty) {

        log.info("operation task: {} split qty: {}", this.id, operatedQty);

        if (operatedQty <= 0) {
            return;
        }

        if (operatedQty == this.requiredQty - this.abnormalQty) {
            complete(operatedQty);
            return;
        }

        this.requiredQty -= operatedQty;
        validateQty();

        sendStockTransferEvent(operatedQty);

        OperationTaskPickingDTO operationTaskPickingDTO = new OperationTaskPickingDTO().setOperatedQty(operatedQty)
                .setDetailId(this.detailId).setOrderId(this.orderId).setTaskType(this.taskType);
        DomainEventPublisher.sendSyncDomainEvent(new PickingOrderPickingEvent().setOperationTasks(Lists.newArrayList(operationTaskPickingDTO)));
    }

    public void reportAbnormal(Integer abnormalQty) {

        log.info("operation task: {} report abnormal qty: {}", this.id, abnormalQty);

        if (abnormalQty > 0 && this.abnormalQty > 0) {
            throw WmsException.throwWmsException(REPEAT_REPORT_STOCK_ABNORMAL);
        }
        this.abnormal = true;
        this.abnormalQty += abnormalQty;
        validateQty();
    }

    public void setActualWorkStation(Long workStationId) {

        log.info("operation task: {} set actual work station:{} ", this.id, workStationId);

        this.workStationId = workStationId;
        this.targetLocationCode = assignedStationSlot.get(workStationId);
    }

    public StockLockTypeEnum transferToLockType() {
        if (taskType == OperationTaskTypeEnum.PICKING) {
            return StockLockTypeEnum.OUTBOUND;
        } else {
            return StockLockTypeEnum.STOCK_MOVE_IN_WAREHOUSE;
        }
    }

    public Long transferToWarehouseAreaId(List<WarehouseAreaDTO> warehouseAreaDTOS) {

        if (CollectionUtils.isEmpty(warehouseAreaDTOS)) {
            return WmsCommonConstants.PICKING_CACHE_STORAGE_WAREHOUSE_ID;
        }

        if (this.taskType == OperationTaskTypeEnum.PICKING
                || this.taskType == OperationTaskTypeEnum.ONE_STEP_RELOCATION
                || this.taskType == OperationTaskTypeEnum.TWO_STEP_RELOCATION) {

            Optional<WarehouseAreaDTO> optional = warehouseAreaDTOS.stream()
                    .filter(v -> v.getWarehouseAreaType() == WarehouseAreaTypeEnum.PICKING_STORAGE_CACHE)
                    .findFirst();
            if (optional.isEmpty()) {
                return WmsCommonConstants.PICKING_CACHE_STORAGE_WAREHOUSE_ID;
            }
            return optional.get().getId();
        }

        return WmsCommonConstants.PUT_AWAY_CACHE_STORAGE_WAREHOUSE_ID;
    }

    private void sendStockTransferEvent(int operatedQty) {
        StockTransferDTO stockTransferDTO = StockTransferDTO.builder()
                .warehouseCode(this.warehouseCode)
                .lockType(this.transferToLockType())
                .containerStockId(this.containerStockId)
                .skuBatchStockId(this.skuBatchStockId)
                .skuBatchAttributeId(this.skuBatchAttributeId)
                .taskId(this.id)
                .skuId(this.skuId)
                .targetContainerCode(this.targetContainerCode)
                .targetContainerFace("")
                .targetContainerSlotCode(StringUtils.isEmpty(this.targetContainerSlotCode) ? "" : this.targetContainerSlotCode)
                .transferQty(operatedQty)
                .warehouseAreaId(this.transferToWarehouseAreaId(Collections.emptyList()))
                .orderNo(this.orderNo)
                .build();

        DomainEventPublisher.sendAsyncDomainEvent(new StockTransferEvent().setStockTransferDTO(stockTransferDTO).setTaskType(this.taskType));
    }
}
