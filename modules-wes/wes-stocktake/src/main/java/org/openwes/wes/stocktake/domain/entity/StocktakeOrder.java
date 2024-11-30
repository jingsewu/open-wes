package org.openwes.wes.stocktake.domain.entity;

import org.openwes.common.utils.id.OrderNoGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.stocktake.constants.*;

import java.util.List;

@Data
@Slf4j
public class StocktakeOrder {

    private Long id;

    private String warehouseCode;

    private String orderNo;

    private String customerOrderNo;

    private StocktakeOrderStatusEnum stocktakeOrderStatus;

    private StocktakeTypeEnum stocktakeType;

    private StocktakeCreateMethodEnum stocktakeCreateMethod;

    private StocktakeMethodEnum stocktakeMethod;

    private StocktakeUnitTypeEnum stocktakeUnitType;

    private Boolean includeZeroStock;

    private Boolean includeEmptySlot;

    private boolean abnormal;

    private Long warehouseAreaId;

    private Long warehouseLogicId;

    private Long version;

    private List<StocktakeOrderDetail> details;

    private List<StocktakeTask> taskList;


    public void initialize() {
        this.stocktakeOrderStatus = StocktakeOrderStatusEnum.NEW;
        this.orderNo = OrderNoGenerator.generationStocktakeOrderNo();
        this.abnormal = false;
        this.details.forEach(StocktakeOrderDetail::initialize);

        log.info("stocktake order: {} orderNo: {} initialize", this.id, this.orderNo);
    }

    public void cancel() {

        log.info("stocktake order: {} orderNo: {} cancel", this.id, this.orderNo);

        if (!StocktakeOrderStatusEnum.isCancelable(this.stocktakeOrderStatus)) {
            throw new IllegalStateException("stock order status is not cancelable");
        }
        this.stocktakeOrderStatus = StocktakeOrderStatusEnum.CANCELED;
    }

    public void execute() {

        log.info("stocktake order: {} orderNo: {} execute", this.id, this.orderNo);

        if (!StocktakeOrderStatusEnum.isExecutable(this.stocktakeOrderStatus)) {
            throw new IllegalStateException("stock order status is not executable");
        }
        this.stocktakeOrderStatus = StocktakeOrderStatusEnum.STARTED;
    }

    public void complete() {

        log.info("stocktake order: {} orderNo: {} complete", this.id, this.orderNo);

        if (StocktakeOrderStatusEnum.isFinal(this.stocktakeOrderStatus)) {
            throw new IllegalStateException("stock order status is final");
        }
        this.stocktakeOrderStatus = StocktakeOrderStatusEnum.DONE;
    }

    public List<Long> getAllStocktakeUnitIds() {
        return this.details.stream().map(StocktakeOrderDetail::getUnitId).toList();
    }

    public List<String> getAllStocktakeUnitCodes() {
        return this.details.stream().map(StocktakeOrderDetail::getUnitCode).toList();
    }
}
