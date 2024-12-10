package org.openwes.wes.basic.container.domain.entity;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.basic.constants.ContainerStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.CONTAINER_SPECIFIC_CANNOT_CHANGE;
import static org.junit.jupiter.api.Assertions.*;

class ContainerTest {

    private Container container;
    private List<ContainerDTO.ContainerSlot> containerSlots;
    private List<ContainerStockDTO> containerStocks;

    @BeforeEach
    public void setUp() {
        containerSlots = new ArrayList<>();
        containerSlots.add(new ContainerDTO.ContainerSlot());
        container = new Container("warehouseCode", "containerCode", "containerSpecCode", containerSlots);
        containerStocks = new ArrayList<>();
    }

    @Test
    void changeContainerSpec_InvalidStatus_ThrowsException() {
        container.setContainerStatus(ContainerStatusEnum.IN_SIDE);
        WmsException exception = assertThrows(WmsException.class, () ->
                container.changeContainerSpec("newSpecCode", containerSlots)
        );
        assertEquals(CONTAINER_SPECIFIC_CANNOT_CHANGE.getCode(), exception.getCode());
    }

    @Test
    void lock_ContainerAlreadyLocked_ThrowsException() {
        container.lock();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                container.lock()
        );
        assertEquals("container is already locked", exception.getMessage());
    }

    @Test
    void changeStocks_EmptyStocks_SetsEmptyContainer() {
        container.changeStocks(containerStocks);
        assertTrue(container.isEmptyContainer());
        assertEquals(container.getContainerSlotNum(), container.getEmptySlotNum());
        assertEquals(BigDecimal.ZERO, container.getOccupationRatio());
    }

    @Test
    void changeStocks_WithStocks_UpdatesContainer() {
        ContainerStockDTO stock = new ContainerStockDTO();
        stock.setTotalQty(1);
        containerStocks.add(stock);

        container.changeStocks(containerStocks);
        assertFalse(container.isEmptyContainer());
        assertEquals(0, container.getEmptySlotNum());
        assertEquals(BigDecimal.ONE.intValue(), container.getOccupationRatio().intValue());
    }

    @Test
    void changeLocation_ValidInput_UpdatesLocationDetails() {
        String newWarehouseCode = "newWarehouseCode";
        Long newWarehouseAreaId = 100L;
        String newLocationCode = "newLocationCode";
        String newLocationType = "newLocationType";

        container.changeLocation(newWarehouseCode, newWarehouseAreaId, newLocationCode, newLocationType);

        assertEquals(newWarehouseCode, container.getWarehouseCode());
        assertEquals(newWarehouseAreaId, container.getWarehouseAreaId());
        assertEquals(newLocationCode, container.getLocationCode());
        assertEquals(newLocationType, container.getLocationType());
    }
}
