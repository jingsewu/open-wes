package org.openwes.wes.inbound.domain.entity;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.inbound.constants.AcceptMethodEnum;
import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.AcceptTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AcceptOrderTest extends BaseTest {

    private AcceptOrder acceptOrder;

    @BeforeEach
    public void setUp() {
        super.setUp();
        acceptOrder = new AcceptOrder();
        acceptOrder.setWarehouseCode("WH001");
        acceptOrder.setAcceptMethod(AcceptMethodEnum.BOX_CONTENT);
        acceptOrder.setAcceptType(AcceptTypeEnum.WITHOUT_ORDER);
        acceptOrder.setPutAway(false);
        acceptOrder.setTotalQty(0);
        acceptOrder.setTotalBox(0);
        acceptOrder.setRemark("Test Remark");
        acceptOrder.setAcceptOrderStatus(AcceptOrderStatusEnum.NEW);
        acceptOrder.setDetails(new ArrayList<>());
    }

    @Test
    void testInitialize_ShouldGenerateOrderNoAndSetStatus() {
        acceptOrder.initialize();
        assertNotNull(acceptOrder.getOrderNo());
        assertEquals(AcceptOrderStatusEnum.NEW, acceptOrder.getAcceptOrderStatus());
    }

    @Test
    void testAccept_ShouldAddDetailAndUpdateTotals() {
        AcceptOrderDetail detail = new AcceptOrderDetail();
        detail.setQtyAccepted(10);
        detail.setBoxNo("BOX001");

        acceptOrder.accept(detail);

        assertEquals(10, acceptOrder.getTotalQty());
        assertEquals(1, acceptOrder.getTotalBox());
        assertEquals(1, acceptOrder.getDetails().size());
    }

    @Test
    void testComplete_ShouldSetStatusToComplete() {
        acceptOrder.complete();
        assertEquals(AcceptOrderStatusEnum.COMPLETE, acceptOrder.getAcceptOrderStatus());
    }

    @Test
    void testComplete_ShouldThrowExceptionIfNotNew() {
        acceptOrder.setAcceptOrderStatus(AcceptOrderStatusEnum.COMPLETE);

        Executable executable = () -> acceptOrder.complete();

        assertThrows(WmsException.class, executable);
    }

    @Test
    void testCancel_ShouldRemoveDetailAndUpdateTotals() {
        AcceptOrderDetail detail = new AcceptOrderDetail();
        detail.setId(1L);
        detail.setQtyAccepted(10);
        detail.setBoxNo("BOX001");

        acceptOrder.getDetails().add(detail);
        acceptOrder.setTotalQty(10);
        acceptOrder.setTotalBox(1);

        acceptOrder.cancel(1L);

        assertEquals(0, acceptOrder.getTotalQty());
        assertEquals(0, acceptOrder.getTotalBox());
    }

    @Test
    void testCancel_ShouldThrowExceptionIfNotNew() {
        acceptOrder.setAcceptOrderStatus(AcceptOrderStatusEnum.COMPLETE);

        Executable executable = () -> acceptOrder.cancel(1L);

        assertThrows(WmsException.class, executable);
    }
}
