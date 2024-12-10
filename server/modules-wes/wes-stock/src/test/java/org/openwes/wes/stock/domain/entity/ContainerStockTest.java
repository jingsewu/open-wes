package org.openwes.wes.stock.domain.entity;

import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerStockTest {

    @Test
    void testValidateQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setAvailableQty(-1);
        containerStock.setTotalQty(10);
        containerStock.setOutboundLockedQty(5);
        containerStock.setNoOutboundLockedQty(2);
        containerStock.setFrozenQty(3);

        try {
            containerStock.validateQty();
        } catch (IllegalStateException e) {
            assertEquals("available qty must be greater than 0", e.getMessage());
        }
    }

    @Test
    void testLockQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(10);
        containerStock.setAvailableQty(10);
        containerStock.setOutboundLockedQty(0);
        containerStock.setNoOutboundLockedQty(0);

        containerStock.lockQty(5, StockLockTypeEnum.OUTBOUND);

        assertEquals(5, containerStock.getAvailableQty());
        assertEquals(5, containerStock.getOutboundLockedQty());
        assertEquals(0, containerStock.getNoOutboundLockedQty());
    }

    @Test
    void testAddQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(10);
        containerStock.setAvailableQty(10);

        containerStock.addQty(5);

        assertEquals(15, containerStock.getTotalQty());
        assertEquals(15, containerStock.getAvailableQty());
    }

    @Test
    void testAddAndLockQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(10);
        containerStock.setAvailableQty(10);
        containerStock.setOutboundLockedQty(0);
        containerStock.setNoOutboundLockedQty(0);

        containerStock.addAndLockQty(5, StockLockTypeEnum.OUTBOUND);

        assertEquals(15, containerStock.getTotalQty());
        assertEquals(10, containerStock.getAvailableQty());
        assertEquals(5, containerStock.getOutboundLockedQty());
    }

    @Test
    void testSubtractQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(15);
        containerStock.setAvailableQty(15);

        containerStock.adjustQty(-10);

        assertEquals(5, containerStock.getTotalQty());
        assertEquals(5, containerStock.getAvailableQty());
    }

    @Test
    void testSubtractAndUnlockQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(15);
        containerStock.setAvailableQty(10);
        containerStock.setOutboundLockedQty(5);
        containerStock.setNoOutboundLockedQty(0);

        containerStock.subtractAndUnlockQty(5, StockLockTypeEnum.OUTBOUND);

        assertEquals(10, containerStock.getTotalQty());
        assertEquals(10, containerStock.getAvailableQty());
        assertEquals(0, containerStock.getNoOutboundLockedQty());
    }

    @Test
    void testFreezeQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(15);
        containerStock.setAvailableQty(15);
        containerStock.setFrozenQty(0);

        containerStock.freezeQty(10);

        assertEquals(15, containerStock.getTotalQty());
        assertEquals(5, containerStock.getAvailableQty());
        assertEquals(10, containerStock.getFrozenQty());
    }

    @Test
    void testUnFreezeQty() {
        ContainerStock containerStock = new ContainerStock();
        containerStock.setTotalQty(15);
        containerStock.setAvailableQty(5);
        containerStock.setFrozenQty(10);

        containerStock.unfreezeQty(5);

        assertEquals(15, containerStock.getTotalQty());
        assertEquals(10, containerStock.getAvailableQty());
        assertEquals(5, containerStock.getFrozenQty());
    }
}
