package org.openwes.wes.stock.domain.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stock.constants.StockAdjustmentOrderStatusEnum;
import org.openwes.wes.api.stock.event.StockAdjustmentOrderCreatedEvent;

import java.util.List;

@Data
@Slf4j
public class StockAdjustmentOrder {

    private Long id;
    private String orderNo;
    private String warehouseCode;

    private String description;

    private List<StockAdjustmentDetail> details;

    private StockAdjustmentOrderStatusEnum status;
    private Long version;

    public StockAdjustmentOrder(String warehouseCode, List<StockAdjustmentDetail> details, String description) {
        this.warehouseCode = warehouseCode;
        this.details = details;
        this.description = description == null ? "" : description;
        this.orderNo = OrderNoGenerator.generationStockAdjustmentOrderNo();
        this.status = StockAdjustmentOrderStatusEnum.NEW;

        DomainEventPublisher.sendAsyncDomainEvent(new StockAdjustmentOrderCreatedEvent().setOrderNo(this.orderNo));
    }

    public void adjust() {
        log.info("stock adjustment order id: {}, orderNo: {} adjust", this.id, this.orderNo);

        if (status != StockAdjustmentOrderStatusEnum.NEW) {
            throw new IllegalStateException("stock adjustment order is not NEW, cannot adjust");
        }

        this.status = StockAdjustmentOrderStatusEnum.COMPLETE;
    }

    public void close() {
        log.info("stock adjustment order id: {}, orderNo: {} close", this.id, this.orderNo);

        if (status != StockAdjustmentOrderStatusEnum.NEW) {
            throw new IllegalStateException("stock adjustment order is not NEW, cannot close");
        }

        this.status = StockAdjustmentOrderStatusEnum.CLOSED;
    }
}
