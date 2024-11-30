package org.openwes.wes.stock.domain.entity;

import com.google.common.base.Preconditions;
import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class ContainerStock extends UpdateUserDTO {


    private Long id;

    private Long skuId;

    private Long skuBatchAttributeId;

    private Long skuBatchStockId;

    /**
     * container is not must be a physical container. e.g.
     * when sku received to a place but not a physical container,
     * then the container code will be received order no ,
     * and when sku put away on the rack, then the container code is the location code;
     */
    private String warehouseCode;
    private Long containerId;
    private String containerCode;
    private String containerFace;
    private String containerSlotCode;

    private Integer totalQty = 0;
    private Integer availableQty = 0;
    // outbound locked qty
    private Integer outboundLockedQty = 0;
    // other operation locked qty in the warehouse
    private Integer noOutboundLockedQty = 0;
    private Integer frozenQty = 0;

    /**
     * it means the container is or not a physical container
     */
    private boolean boxStock;
    private String boxNo;

    private Long version;

    public void validateQty() {
        Preconditions.checkState(this.availableQty >= 0, "available qty must be greater than 0");
        Preconditions.checkState(this.totalQty >= 0, "total qty must be greater than 0");
        Preconditions.checkState(this.outboundLockedQty >= 0, "outbound lock qty must be greater than 0");
        Preconditions.checkState(this.noOutboundLockedQty >= 0, "no outbound lock qty must be greater than 0");
        Preconditions.checkState(this.totalQty == this.availableQty + this.outboundLockedQty
                        + this.noOutboundLockedQty + this.frozenQty,
                "total qty must equals availableQty + noOutboundLockedQty + outboundLockedQty + frozenQty");
    }

    public void lockQty(Integer lockQty, StockLockTypeEnum stockLockType) {
        log.info("container stock: {} lock qty: {} with lockType: {}", id, lockQty, stockLockType);
        this.availableQty -= lockQty;
        if (stockLockType == StockLockTypeEnum.OUTBOUND) {
            this.outboundLockedQty += lockQty;
        } else {
            this.noOutboundLockedQty += lockQty;
        }
        validateQty();
    }

    public void addQty(Integer addQty) {
        log.info("container stock: {} add qty: {}", id, addQty);
        this.totalQty += addQty;
        this.availableQty += addQty;
        validateQty();
    }

    public void addAndLockQty(Integer addQty, StockLockTypeEnum stockLockType) {
        log.info("container stock: {} add and lock  qty: {} with lockType: {}", id, addQty, stockLockType);
        this.totalQty += addQty;
        if (stockLockType == StockLockTypeEnum.OUTBOUND) {
            this.outboundLockedQty += addQty;
        } else {
            this.noOutboundLockedQty += addQty;
        }
        validateQty();
    }

    public void subtractQty(Integer subtractQty) {
        log.info("container stock: {} subtract qty: {}", id, subtractQty);
        this.totalQty -= subtractQty;
        this.availableQty -= subtractQty;
        validateQty();
    }

    public void subtractAndUnlockQty(Integer subtractQty, StockLockTypeEnum stockLockType) {
        log.info("container stock: {} subtract and unlock qty: {} with lockType: {}", id, subtractQty, stockLockType);
        this.totalQty -= subtractQty;
        if (stockLockType == StockLockTypeEnum.OUTBOUND) {
            this.outboundLockedQty -= subtractQty;
        } else {
            this.noOutboundLockedQty -= subtractQty;
        }
        validateQty();
    }

    public void freezeQty(Integer freezeQty) {
        log.info("container stock: {} freeze qty: {}", id, freezeQty);
        this.frozenQty += freezeQty;
        this.availableQty -= freezeQty;
        validateQty();
    }

    public void unfreezeQty(Integer unfreezeQty) {
        log.info("container stock: {} unfreeze qty: {}", id, unfreezeQty);
        this.frozenQty -= unfreezeQty;
        this.availableQty += unfreezeQty;
        validateQty();
    }

    public void adjustQty(Integer adjustQty) {
        log.info("container stock: {} adjust qty: {}", id, adjustQty);

        this.availableQty += adjustQty;
        this.totalQty += adjustQty;
        validateQty();
    }
}
