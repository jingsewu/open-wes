package org.openwes.wes.stock.domain.entity;

import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.stock.constants.StockAbnormalReasonEnum;
import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.api.stock.constants.StockAbnormalTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class StockAbnormalRecord extends UpdateUserDTO {

    private Long id;

    private String warehouseCode;

    private String ownerCode;

    private String orderNo;

    private StockAbnormalTypeEnum stockAbnormalType;

    private String replayNo;

    private Long containerStockId;
    private Long skuBatchStockId;
    private Long skuBatchAttributeId;
    private Long skuId;
    private String skuCode;

    private String containerCode;
    private String containerSlotCode;
    private String locationCode;

    private Integer qtyAbnormal;

    private StockAbnormalReasonEnum abnormalReason;
    private String reasonDesc;
    private String abnormalOrderNo;
    private StockAbnormalStatusEnum stockAbnormalStatus = StockAbnormalStatusEnum.NEW;
    private Long version;

    public void initial() {
        this.orderNo = OrderNoGenerator.generationStockAbnormalOrderNo();
    }

    public void createAdjustmentOrder() {
        log.info("stock abnormal record id: {}, orderNo: {} create adjustment order", this.id, this.orderNo);

        if (this.stockAbnormalStatus != StockAbnormalStatusEnum.NEW) {
            throw new IllegalStateException("order status is not NEW, can not create adjustment order");
        }
        this.stockAbnormalStatus = StockAbnormalStatusEnum.CREATE_ADJUSTMENT_ORDER;
    }

    public void upstreamClose() {
        log.info("stock abnormal record id: {}, orderNo: {} upstream close", this.id, this.orderNo);

        if (this.stockAbnormalStatus != StockAbnormalStatusEnum.CREATE_ADJUSTMENT_ORDER
            && this.stockAbnormalStatus != StockAbnormalStatusEnum.CREATE_RE_CHECK_ORDER) {
            throw new IllegalStateException("order status is not CREATE_ADJUSTMENT_ORDER or CREATE_RE_CHECK_ORDER , can not be closed");
        }
        this.stockAbnormalStatus = StockAbnormalStatusEnum.CLOSED;
    }

    public void manualClose() {
        log.info("stock abnormal record id: {}, orderNo: {} manual close", this.id, this.orderNo);

        if (this.stockAbnormalStatus != StockAbnormalStatusEnum.NEW) {
            throw new IllegalStateException("order status is not new, can not be closed");
        }
        this.stockAbnormalStatus = StockAbnormalStatusEnum.CLOSED;
    }

    public void recheckClose(String replayNo) {
        log.info("stock abnormal record id: {}, orderNo: {} recheck close", this.id, this.orderNo);

        if (this.stockAbnormalStatus != StockAbnormalStatusEnum.NEW
                && this.stockAbnormalStatus != StockAbnormalStatusEnum.CREATE_RE_CHECK_ORDER) {
            throw new IllegalStateException("order status is not NEW or CREATE_RE_CHECK_ORDER, can not be closed");
        }
        this.stockAbnormalStatus = StockAbnormalStatusEnum.CLOSED;
        if (StringUtils.isNotEmpty(replayNo)) {
            this.replayNo = replayNo;
        }
    }

    public void completeAdjustment() {
        log.info("stock abnormal record id: {}, orderNo: {} complete adjustment", this.id, this.orderNo);

        if (this.stockAbnormalStatus != StockAbnormalStatusEnum.CREATE_ADJUSTMENT_ORDER) {
            throw new IllegalStateException("order status is not CREATE_ADJUSTMENT_ORDER, can not complete adjust");
        }
        this.stockAbnormalStatus = StockAbnormalStatusEnum.FINISH_ADJUSTMENT_ORDER;
    }

    public void createRecheckOrder(String replayNo) {
        log.info("stock abnormal record id: {}, orderNo: {} create recheck order, replyNo: {}", this.id, this.orderNo, replayNo);

        if (this.stockAbnormalStatus != StockAbnormalStatusEnum.NEW) {
            throw new IllegalStateException("order status is not NEW, can not create recheck order");
        }
        this.stockAbnormalStatus = StockAbnormalStatusEnum.CREATE_RE_CHECK_ORDER;
        this.replayNo = replayNo;
    }


}
