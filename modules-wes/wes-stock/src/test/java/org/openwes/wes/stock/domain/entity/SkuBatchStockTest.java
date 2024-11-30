package org.openwes.wes.stock.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SkuBatchStockTest {

    @Test
    void testSetWarehouseAreaId() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        Long warehouseAreaId = 1L;

        skuBatchStock.setWarehouseAreaId(warehouseAreaId);

        assertNotNull(skuBatchStock.getWarehouseAreaId());
        assertEquals(warehouseAreaId, skuBatchStock.getWarehouseAreaId());
    }

    @Test
    void testValidateQty() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(-1);
        skuBatchStock.setAvailableQty(-1);
        skuBatchStock.setOutboundLockedQty(-1);
        skuBatchStock.setNoOutboundLockedQty(-1);

        Assertions.assertThrows(IllegalStateException.class, skuBatchStock::validateQty);
    }

    @Test
    void testLockQty() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(10);
        skuBatchStock.setAvailableQty(10);
        skuBatchStock.setOutboundLockedQty(0);
        skuBatchStock.setNoOutboundLockedQty(0);

        Integer lockQty = 3;
        StockLockTypeEnum stockLockType = StockLockTypeEnum.OUTBOUND;

        skuBatchStock.lockQty(lockQty, stockLockType);

        assertEquals(skuBatchStock.getAvailableQty(), 7);
        assertEquals(skuBatchStock.getOutboundLockedQty(), 3);
        assertEquals(skuBatchStock.getNoOutboundLockedQty(), 0);
    }

    @Test
    void testAddQty() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(10);
        skuBatchStock.setAvailableQty(10);

        Integer addQty = 5;

        skuBatchStock.addQty(addQty);

        assertEquals(skuBatchStock.getTotalQty(), 15);
        assertEquals(skuBatchStock.getAvailableQty(), 15);
    }

    @Test
    void testAddAndLockQty() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(10);
        skuBatchStock.setAvailableQty(10);
        skuBatchStock.setOutboundLockedQty(0);
        skuBatchStock.setNoOutboundLockedQty(0);

        Integer addQty = 5;
        StockLockTypeEnum stockLockType = StockLockTypeEnum.OUTBOUND;

        skuBatchStock.addAndLockQty(addQty, stockLockType);

        assertEquals(skuBatchStock.getTotalQty(), 15);
        assertEquals(skuBatchStock.getAvailableQty(), 10);
        assertEquals(skuBatchStock.getOutboundLockedQty(), 5);
        assertEquals(skuBatchStock.getNoOutboundLockedQty(), 0);
    }

    @Test
    void testSubtractQty() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(15);
        skuBatchStock.setAvailableQty(15);

        Integer subtractQty = 5;

        skuBatchStock.adjustQty(-subtractQty);

        assertEquals(skuBatchStock.getTotalQty(), 10);
        assertEquals(skuBatchStock.getAvailableQty(), 10);
    }

    @Test
    void testSubtractAndUnlockQty() {
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(15);
        skuBatchStock.setAvailableQty(10);
        skuBatchStock.setOutboundLockedQty(5);
        skuBatchStock.setNoOutboundLockedQty(0);

        Integer subtractQty = 5;
        StockLockTypeEnum stockLockType = StockLockTypeEnum.OUTBOUND;

        skuBatchStock.subtractAndUnlockQty(subtractQty, stockLockType);

        assertEquals(skuBatchStock.getTotalQty(), 10);
        assertEquals(skuBatchStock.getAvailableQty(), 10);
        assertEquals(skuBatchStock.getOutboundLockedQty(), 0);
        assertEquals(skuBatchStock.getNoOutboundLockedQty(), 0);
    }
}
