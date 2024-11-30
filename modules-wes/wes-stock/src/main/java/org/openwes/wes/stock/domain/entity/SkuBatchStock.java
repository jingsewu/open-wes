package org.openwes.wes.stock.domain.entity;

import com.google.common.base.Preconditions;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * stock design rule:
 * 1. stock created by receiving;
 * 2. stock transfer from one area to another area in warehouse;
 * 3. stock subtraction by shipping. if our system don't contain shipping module, then scheduled delete shipping area stock;
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class SkuBatchStock extends UpdateUserPO {

    private Long id;

    private Long skuId;

    //unique key union skuBatchAttributeId and warehouseAreaCode and warehouseCode
    private String warehouseCode;
    private Long skuBatchAttributeId;
    private Long warehouseAreaId;

    private Integer totalQty = 0;
    private Integer availableQty = 0;
    // outbound locked qty
    private Integer outboundLockedQty = 0;
    // other operation locked qty in the warehouse
    private Integer noOutboundLockedQty = 0;
    private Integer frozenQty = 0;
    private Long version;

    public void setWarehouseAreaId(Long warehouseAreaId) {
        Preconditions.checkState(warehouseAreaId != null && warehouseAreaId >= 0, "warehouseAreaId cannot be empty");
        this.warehouseAreaId = warehouseAreaId;
    }

    public void validateQty() {
        Preconditions.checkState(this.availableQty >= 0, "available qty must be greater than 0");
        Preconditions.checkState(this.totalQty >= 0, "total qty must be greater than 0");
        Preconditions.checkState(this.outboundLockedQty >= 0, "outbound lock qty must be greater than 0");
        Preconditions.checkState(this.noOutboundLockedQty >= 0, "no outbound lock qty must be greater than 0");
        Preconditions.checkState(this.frozenQty >= 0, "frozen qty must be greater than 0");
        Preconditions.checkState(this.totalQty == this.availableQty + this.outboundLockedQty + this.noOutboundLockedQty + this.frozenQty,
            "total qty must equals availableQty + noOutboundLockedQty + outboundLockedQty +frozenQty");
    }

    public void lockQty(Integer lockQty, StockLockTypeEnum stockLockType) {
        log.info("sku batch stock: {} lock qty: {} with lockType: {}", id, lockQty, stockLockType);
        this.availableQty -= lockQty;
        if (stockLockType == StockLockTypeEnum.OUTBOUND) {
            this.outboundLockedQty += lockQty;
        } else {
            this.noOutboundLockedQty += lockQty;
        }
        validateQty();
    }

    public void addQty(Integer addQty) {
        log.info("sku batch stock: {} add qty: {}", id, addQty);

        this.totalQty += addQty;
        this.availableQty += addQty;
        validateQty();
    }

    public void addAndLockQty(Integer addQty, StockLockTypeEnum stockLockType) {
        log.info("sku batch stock: {} add and lock qty: {} with lockType: {}", id, addQty, stockLockType);

        this.totalQty += addQty;
        if (stockLockType == StockLockTypeEnum.OUTBOUND) {
            this.outboundLockedQty += addQty;
        } else {
            this.noOutboundLockedQty += addQty;
        }
        validateQty();
    }

    public void subtractAndUnlockQty(Integer subtractQty, StockLockTypeEnum stockLockType) {
        log.info("sku batch stock: {} subtract and unlock qty: {} with lockType: {}", id, subtractQty, stockLockType);

        this.totalQty -= subtractQty;
        if (stockLockType == StockLockTypeEnum.OUTBOUND) {
            this.outboundLockedQty -= subtractQty;
        } else {
            this.noOutboundLockedQty -= subtractQty;
        }
        validateQty();
    }

    public void freezeQty(Integer freezeQty) {
        log.info("sku batch stock: {} freeze qty: {}", id, freezeQty);
        this.frozenQty += freezeQty;
        this.availableQty -= freezeQty;
        validateQty();
    }

    public void unfreezeQty(Integer unfreezeQty) {
        log.info("sku batch stock: {} unfreeze qty: {}", id, unfreezeQty);
        this.frozenQty -= unfreezeQty;
        this.availableQty += unfreezeQty;
        validateQty();
    }

    public void adjustQty(Integer adjustQty) {
        log.info("sku batch stock: {} adjust qty: {}", id, adjustQty);

        this.availableQty += adjustQty;
        this.totalQty += adjustQty;
        validateQty();
    }
}
