package org.openwes.wes.stocktake.domain.entity;

import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stocktake.constants.StocktakeAbnormalReasonEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordSubmitDTO;
import org.openwes.wes.api.stocktake.event.StocktakeRecordSubmitEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Data
public class StocktakeRecord {

    private Long id;

    private Long stocktakeOrderId;

    private Long stocktakeTaskId;

    private Long stocktakeTaskDetailId;

    private StocktakeRecordStatusEnum stocktakeRecordStatus;

    private String warehouseCode;

    private Long containerId;

    private String containerCode;

    private String containerFace;

    private String containerSlotCode;

    private Long skuId;

    private Long stockId;

    private Long skuBatchAttributeId;

    private Long skuBatchStockId;

    private Integer qtyOriginal;

    private Integer qtyStocktake;

    private Long workStationId;

    private StocktakeAbnormalReasonEnum abnormalReason;

    private Long version;

    public int getQtyAbnormal() {
        return Optional.ofNullable(qtyStocktake).orElse(0) - Optional.ofNullable(qtyOriginal).orElse(0);
    }

    public void initialize(StocktakeTaskDetail taskDetail) {
        this.stocktakeOrderId = taskDetail.getStocktakeOrderId();
        this.stocktakeTaskId = taskDetail.getStocktakeTaskId();
        this.stocktakeTaskDetailId = taskDetail.getId();
        this.qtyStocktake = 0;
        this.stocktakeRecordStatus = StocktakeRecordStatusEnum.NEW;

        log.info("stocktake order: {}  stocktake task: {} stocktake record: {} initialize", this.stocktakeOrderId, this.stocktakeTaskId, this.id);
    }

    public void submit(StocktakeRecordSubmitDTO submitDTO) {

        log.info("stocktake order: {}  stocktake task: {} stocktake record: {} submit qty: {}", this.stocktakeOrderId, this.stocktakeTaskId, this.id, submitDTO.getStocktakeQty());

        if (this.stocktakeRecordStatus != StocktakeRecordStatusEnum.NEW) {
            throw new IllegalStateException("record status is not NEW, can not submit stocktake record");
        }
        this.qtyStocktake = submitDTO.getStocktakeQty();
        this.workStationId = submitDTO.getWorkStationId();
        int qtyAbnormal = getQtyAbnormal();
        if (qtyAbnormal < 0) {
            this.abnormalReason = StocktakeAbnormalReasonEnum.LESS_ENTITY;
        } else if (qtyAbnormal > 0) {
            this.abnormalReason = StocktakeAbnormalReasonEnum.MORE_ENTITY;
        } else {
            this.abnormalReason = StocktakeAbnormalReasonEnum.NONE;
        }
        this.stocktakeRecordStatus = StocktakeRecordStatusEnum.DONE;

        DomainEventPublisher.sendAsyncDomainEvent(new StocktakeRecordSubmitEvent()
                .setStocktakeRecordId(this.id).setStocktakeTaskDetailId(this.stocktakeTaskDetailId)
                .setStocktakeTaskId(this.stocktakeTaskId));
    }

    public void close() {

        log.info("stocktake order: {}  stocktake task: {} stocktake record: {} close", this.stocktakeOrderId, this.stocktakeTaskId, this.id);

        if (this.stocktakeRecordStatus != StocktakeRecordStatusEnum.NEW) {
            throw new IllegalStateException("record status is not NEW, can not close stocktake record");
        }
        this.stocktakeRecordStatus = StocktakeRecordStatusEnum.CLOSE;
    }
}
