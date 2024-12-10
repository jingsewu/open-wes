package org.openwes.wes.inbound.domain.entity;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InboundPlanOrderTest extends BaseTest {

    @InjectMocks
    private InboundPlanOrder inboundPlanOrder;

    @Mock
    private InboundPlanOrderDetail inboundPlanOrderDetail;

    @BeforeEach
    public void setUp() {
        super.setUp();
        inboundPlanOrder = new InboundPlanOrder();
        inboundPlanOrder.setInboundPlanOrderStatus(InboundPlanOrderStatusEnum.NEW);
        inboundPlanOrder.setDetails(new ArrayList<>());
        inboundPlanOrder.getDetails().add(inboundPlanOrderDetail);
    }

    @Test
    void initialize_WhenDetailsProvided_ShouldSetTotalBoxAndQtyCorrectly() {
        InboundPlanOrderDetail detail1 = mock(InboundPlanOrderDetail.class);
        InboundPlanOrderDetail detail2 = mock(InboundPlanOrderDetail.class);
        when(detail1.getSkuCode()).thenReturn("sku1");
        when(detail2.getSkuCode()).thenReturn("sku2");
        when(detail1.getQtyRestocked()).thenReturn(10);
        when(detail2.getQtyRestocked()).thenReturn(20);
        when(detail1.getBoxNo()).thenReturn("box1");
        when(detail2.getBoxNo()).thenReturn(null);

        inboundPlanOrder.setDetails(Arrays.asList(detail1, detail2));
        inboundPlanOrder.initialize();

        assertEquals(1, inboundPlanOrder.getTotalBox());
        assertEquals(30, inboundPlanOrder.getTotalQty());
        assertEquals(2, inboundPlanOrder.getSkuKindNum());
    }

    @Test
    void accept_WhenOrderStatusIsNew_ShouldChangeStatusToAccepting() {
        AcceptRecordDTO acceptRecordDTO = new AcceptRecordDTO();
        acceptRecordDTO.setQtyAccepted(10);
        acceptRecordDTO.setQtyAbnormal(0);
        acceptRecordDTO.setInboundPlanOrderDetailId(1L);

        when(inboundPlanOrderDetail.getId()).thenReturn(1L);

        inboundPlanOrder.accept(acceptRecordDTO);

        assertEquals(InboundPlanOrderStatusEnum.ACCEPTING, inboundPlanOrder.getInboundPlanOrderStatus());
    }

    @Test
    void close_WhenOrderStatusIsNotNewOrAccepting_ShouldThrowException() {
        inboundPlanOrder.setInboundPlanOrderStatus(InboundPlanOrderStatusEnum.ACCEPTED);

        assertThrows(WmsException.class, () -> inboundPlanOrder.close());
    }

    @Test
    void cancel_WhenOrderStatusIsNotNew_ShouldThrowException() {
        inboundPlanOrder.setInboundPlanOrderStatus(InboundPlanOrderStatusEnum.ACCEPTING);

        assertThrows(WmsException.class, () -> inboundPlanOrder.cancel());
    }

    @Test
    void isFullAccepted_WhenAllDetailsAccepted_ShouldReturnTrue() {
        when(inboundPlanOrderDetail.isCompleted()).thenReturn(true);

        boolean result = inboundPlanOrder.isFullAccepted();

        assertTrue(result);
    }

    @Test
    void completeAccepted_WhenNotAllDetailsAccepted_ShouldNotChangeStatus() {
        when(inboundPlanOrderDetail.isCompleted()).thenReturn(false);

        inboundPlanOrder.completeAccepted();

        assertEquals(InboundPlanOrderStatusEnum.NEW, inboundPlanOrder.getInboundPlanOrderStatus());
    }
}
