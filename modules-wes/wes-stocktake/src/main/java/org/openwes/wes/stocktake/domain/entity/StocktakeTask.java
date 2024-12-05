package org.openwes.wes.stocktake.domain.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stocktake.constants.*;
import org.openwes.wes.api.stocktake.event.StocktakeTaskCloseEvent;

import java.util.List;
import java.util.Objects;

@Data
@Slf4j
public class StocktakeTask {

    private Long id;

    private Long stocktakeOrderId;

    private String taskNo;

    private StocktakeTypeEnum stocktakeType;

    private StocktakeCreateMethodEnum stocktakeCreateMethod;

    private StocktakeMethodEnum stocktakeMethod;

    private StocktakeUnitTypeEnum stocktakeUnitType;

    private StocktakeTaskStatusEnum stocktakeTaskStatus;

    private String warehouseCode;

    private Long workStationId;

    private Long receivedUserId;

    private Long version;

    private List<StocktakeTaskDetail> details;

    public void initialize(String stocktakeOrderNo, int index) {
        this.stocktakeTaskStatus = StocktakeTaskStatusEnum.NEW;
        this.taskNo = String.format("%s-%d", stocktakeOrderNo, index);
        this.details.forEach(StocktakeTaskDetail::initialize);

        log.info("stocktake order: {} stocktake task: {} initialized", this.stocktakeOrderId, this.taskNo);
    }

    public void receive(Long workStationId) {

        log.info("stocktake order: {} stocktake task: {} received with work station id: {}", this.stocktakeOrderId,
                this.taskNo, workStationId);

        if (this.stocktakeTaskStatus != StocktakeTaskStatusEnum.NEW) {
            throw new IllegalStateException("stocktake task status is not new, can not be received");
        }

        this.stocktakeTaskStatus = StocktakeTaskStatusEnum.STARTED;
        this.workStationId = workStationId;
    }

    public void submit(Long stocktakeTaskDetailId) {

        log.info("stocktake order: {} stocktake task: {} submit stocktake task detail id: {}", this.stocktakeOrderId,
                this.taskNo, stocktakeTaskDetailId);

        this.details.stream().filter(v -> Objects.equals(v.getId(), stocktakeTaskDetailId))
                .forEach(StocktakeTaskDetail::complete);

        if (this.details.stream().allMatch(StocktakeTaskDetail::isCompleted)) {
            this.stocktakeTaskStatus = StocktakeTaskStatusEnum.DONE;
        }
    }

    public void close() {

        log.info("stocktake order: {} stocktake task: {} closed", this.stocktakeOrderId, this.taskNo);

        if (!StocktakeTaskStatusEnum.isCloseable(this.stocktakeTaskStatus)) {
            throw new IllegalStateException("stocktake task status is not new or started, can not be closed");
        }

        this.stocktakeTaskStatus = StocktakeTaskStatusEnum.CLOSE;

        DomainEventPublisher.sendAsyncDomainEvent(new StocktakeTaskCloseEvent().setStocktakeTaskId(this.id).setStocktakeOrderId(this.stocktakeOrderId));

        this.details.stream().filter(v -> v.getStocktakeTaskDetailStatus() == StocktakeTaskDetailStatusEnum.NEW
                        || v.getStocktakeTaskDetailStatus() == StocktakeTaskDetailStatusEnum.STARTED)
                .forEach(StocktakeTaskDetail::close);
    }
}
