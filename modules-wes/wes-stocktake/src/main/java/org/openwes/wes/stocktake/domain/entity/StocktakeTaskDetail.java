package org.openwes.wes.stocktake.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskDetailStatusEnum;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class StocktakeTaskDetail extends UpdateUserDTO {

    private Long id;

    private Long stocktakeOrderId;

    private Long stocktakeTaskId;

    private String warehouseCode;

    private String containerCode;

    private String containerFace;

    private StocktakeTaskDetailStatusEnum stocktakeTaskDetailStatus;

    private Long version;

    private List<StocktakeRecord> recordList;

    public void initialize() {
        this.stocktakeTaskDetailStatus = StocktakeTaskDetailStatusEnum.NEW;
    }

    public void generateRecords() {

        log.info("stocktake order: {} stocktake task: {}  stocktake detail: {} generate records", this.stocktakeOrderId, this.stocktakeTaskId, this.id);

        if (this.stocktakeTaskDetailStatus != StocktakeTaskDetailStatusEnum.NEW) {
            throw new IllegalStateException("stocktake task detail can not generate records, illegal status: " + this.stocktakeTaskDetailStatus);
        }
        this.stocktakeTaskDetailStatus = StocktakeTaskDetailStatusEnum.STARTED;
    }

    public void complete() {

        log.info("stocktake order: {} stocktake task: {}  stocktake detail: {} complete", this.stocktakeOrderId, this.stocktakeTaskId, this.id);

        if (this.stocktakeTaskDetailStatus != StocktakeTaskDetailStatusEnum.STARTED) {
            throw new IllegalStateException("stocktake task detail can not complete illegal status: " + this.stocktakeTaskDetailStatus);
        }
        this.stocktakeTaskDetailStatus = StocktakeTaskDetailStatusEnum.DONE;
    }

    public boolean isCompleted() {
        return this.stocktakeTaskDetailStatus == StocktakeTaskDetailStatusEnum.DONE
                || this.stocktakeTaskDetailStatus == StocktakeTaskDetailStatusEnum.CLOSE;
    }

    public void close() {

        log.info("stocktake order: {} stocktake task: {}  stocktake detail: {} close", this.stocktakeOrderId, this.stocktakeTaskId, this.id);

        if (!StocktakeTaskDetailStatusEnum.isCloseable(this.stocktakeTaskDetailStatus)) {
            throw new IllegalStateException("stocktake task detail can not close, illegal status: " + this.stocktakeTaskDetailStatus);
        }
        this.stocktakeTaskDetailStatus = StocktakeTaskDetailStatusEnum.CLOSE;
    }
}
