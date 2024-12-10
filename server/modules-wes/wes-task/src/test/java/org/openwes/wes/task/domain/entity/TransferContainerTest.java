package org.openwes.wes.task.domain.entity;

import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferContainerTest {

    private TransferContainer transferContainer;

    @BeforeEach
    public void setUp() {
        transferContainer = new TransferContainer();
        transferContainer.setId(1L);
        transferContainer.setTransferContainerCode("TC001");
        transferContainer.setTransferContainerStatus(TransferContainerStatusEnum.IDLE);
    }

    @Test
    void occupy_WhenStatusIsIdle_ShouldSetStatusToOccupancy() {
        Long recordId = 10L;

        transferContainer.occupy(recordId);

        assertEquals(TransferContainerStatusEnum.OCCUPANCY, transferContainer.getTransferContainerStatus());
        assertEquals(1, transferContainer.getCurrentPeriodRelateRecordIds().size());
        assertTrue(transferContainer.getCurrentPeriodRelateRecordIds().contains(recordId));
    }

    @Test
    void occupy_WhenStatusIsNotIdle_ShouldThrowException() {
        transferContainer.setTransferContainerStatus(TransferContainerStatusEnum.OCCUPANCY);
        Long recordId = 10L;

        assertThrows(IllegalStateException.class, () -> {
            transferContainer.occupy(recordId);
        });
    }

    @Test
    void forceOccupy_WhenStatusIsIdle_ShouldThrowException() {
        Long recordId = 10L;

        assertThrows(IllegalStateException.class, () -> {
            transferContainer.forceOccupy(recordId);
        });
    }

    @Test
    void forceOccupy_WhenStatusIsOccupancy_ShouldAddRecordId() {
        transferContainer.setTransferContainerStatus(TransferContainerStatusEnum.IDLE);
        Long recordId1 = 10L;
        Long recordId2 = 20L;

        transferContainer.occupy(recordId1);
        transferContainer.forceOccupy(recordId2);

        assertEquals(2, transferContainer.getCurrentPeriodRelateRecordIds().size());
        assertTrue(transferContainer.getCurrentPeriodRelateRecordIds().contains(recordId1));
        assertTrue(transferContainer.getCurrentPeriodRelateRecordIds().contains(recordId2));
    }

    @Test
    void unOccupy_WhenStatusIsOccupancy_ShouldSetStatusToIdle() {
        Long recordId = 10L;
        transferContainer.occupy(recordId);

        transferContainer.unOccupy();

        assertEquals(TransferContainerStatusEnum.IDLE, transferContainer.getTransferContainerStatus());
    }

    @Test
    void unOccupy_WhenStatusIsNotOccupancy_ShouldThrowException() {
        transferContainer.setTransferContainerStatus(TransferContainerStatusEnum.LOCKED);

        assertThrows(IllegalStateException.class, () -> {
            transferContainer.unOccupy();
        });
    }

    @Test
    void lock_WhenStatusIsOccupancy_ShouldSetStatusToLocked() {
        Long recordId = 10L;
        transferContainer.occupy(recordId);

        transferContainer.lock();

        assertEquals(TransferContainerStatusEnum.LOCKED, transferContainer.getTransferContainerStatus());
        assertNotNull(transferContainer.getLockedTime());
    }

    @Test
    void lock_WhenStatusIsNotOccupancy_ShouldThrowException() {
        transferContainer.setTransferContainerStatus(TransferContainerStatusEnum.IDLE);

        assertThrows(IllegalStateException.class, () -> {
            transferContainer.lock();
        });
    }

    @Test
    void unlock_WhenStatusIsLocked_ShouldSetStatusToIdleAndClearFields() {
        Long recordId = 10L;
        transferContainer.occupy(recordId);
        transferContainer.lock();

        transferContainer.unlock();

        assertEquals(TransferContainerStatusEnum.IDLE, transferContainer.getTransferContainerStatus());
        assertNull(transferContainer.getWarehouseAreaId());
        assertEquals("", transferContainer.getLocationCode());
        assertNull(transferContainer.getCurrentPeriodRelateRecordIds());
        assertEquals(0L, transferContainer.getLockedTime().longValue());
    }

    @Test
    void unlock_WhenStatusIsNotLocked_ShouldThrowException() {
        transferContainer.setTransferContainerStatus(TransferContainerStatusEnum.OCCUPANCY);

        assertThrows(IllegalStateException.class, () -> {
            transferContainer.unlock();
        });
    }

    @Test
    void updateLocation_WhenCalled_ShouldUpdateLocationFields() {
        Long warehouseAreaId = 100L;
        String locationCode = "A1";

        transferContainer.updateLocation(warehouseAreaId, locationCode);

        assertEquals(warehouseAreaId, transferContainer.getWarehouseAreaId());
        assertEquals(locationCode, transferContainer.getLocationCode());
    }
}
