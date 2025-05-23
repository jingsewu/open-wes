package org.openwes.wes.stocktake.domain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stocktake.constants.*;
import org.openwes.wes.api.stocktake.event.StocktakeTaskCloseEvent;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public static StocktakeTask createFromOrder(@NotNull StocktakeOrder stocktakeOrder,
                                                Map<String, Set<String>> containerFaceMap,
                                                List<String> subContainerCodes
    ) {

        Assert.notNull(stocktakeOrder, "stocktake order must not be null");
        Assert.notNull(containerFaceMap, "container face map must not be null");
        Assert.notEmpty(containerFaceMap, "container face map must not be empty");

        List<StocktakeTaskDetail> stocktakeTaskDetails = subContainerCodes.stream().flatMap(containerCode ->
                containerFaceMap.get(containerCode).stream().map(containerFace -> {
                    StocktakeTaskDetail stocktakeTaskDetail = new StocktakeTaskDetail();
                    stocktakeTaskDetail.setContainerCode(containerCode);
                    stocktakeTaskDetail.setStocktakeOrderId(stocktakeOrder.getId());
                    stocktakeTaskDetail.setContainerFace(containerFace);
                    stocktakeTaskDetail.setStocktakeTaskDetailStatus(StocktakeTaskDetailStatusEnum.NEW);
                    stocktakeTaskDetail.setWarehouseCode(stocktakeOrder.getWarehouseCode());
                    return stocktakeTaskDetail;
                }).toList().stream()).toList();
        return buildStocktakeTask(stocktakeOrder, stocktakeTaskDetails);
    }

    private static StocktakeTask buildStocktakeTask(StocktakeOrder stocktakeOrder,
                                                    List<StocktakeTaskDetail> stocktakeTaskDetails) {
        StocktakeTask stocktakeTask = new StocktakeTask();
        stocktakeTask.setStocktakeMethod(stocktakeOrder.getStocktakeMethod());
        stocktakeTask.setStocktakeUnitType(stocktakeOrder.getStocktakeUnitType());
        stocktakeTask.setStocktakeType(stocktakeOrder.getStocktakeType());
        stocktakeTask.setStocktakeCreateMethod(stocktakeOrder.getStocktakeCreateMethod());
        stocktakeTask.setStocktakeOrderId(stocktakeOrder.getId());
        stocktakeTask.setStocktakeTaskStatus(StocktakeTaskStatusEnum.NEW);
        stocktakeTask.setWarehouseCode(stocktakeOrder.getWarehouseCode());
        stocktakeTask.setDetails(stocktakeTaskDetails);
        return stocktakeTask;
    }

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
