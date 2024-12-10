package org.openwes.wes.stock.domain.service.impl;

import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.openwes.wes.stock.domain.transfer.ContainerStockTransferImpl;
import org.openwes.wes.stock.domain.transfer.SkuBatchStockTransferImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class StockServiceImplTest {

    private StockServiceImpl stockService;

    @Mock
    private ContainerStockRepository containerStockRepository;

    @Mock
    private SkuBatchStockRepository skuBatchStockRepository;

    @BeforeEach
    void setUp() {
        stockService = new StockServiceImpl(containerStockRepository, skuBatchStockRepository,
                new SkuBatchStockTransferImpl(), new ContainerStockTransferImpl());
    }

    @Test
    void testTransferContainerStock() {
        // Create test data
        ContainerStock containerStock = new ContainerStock();
        containerStock.setContainerCode("container1");
        containerStock.setContainerSlotCode("slot1");
        containerStock.setSkuBatchStockId(1L);
        containerStock.setAvailableQty(5);
        containerStock.setTotalQty(5);
        containerStock.setFrozenQty(0);
        containerStock.setOutboundLockedQty(0);
        containerStock.setNoOutboundLockedQty(0);

        StockTransferDTO stockTransferDTO = new StockTransferDTO();
        stockTransferDTO.setTargetContainerCode("targetContainer1");
        stockTransferDTO.setTargetContainerSlotCode("targetSlot1");
        stockTransferDTO.setSkuBatchStockId(1L);
        stockTransferDTO.setTransferQty(5);

        ContainerStock targetContainerStock = new ContainerStock();
        when(containerStockRepository.findByContainerAndSlotAndSkuBatch(
                stockTransferDTO.getTargetContainerCode(),
                stockTransferDTO.getTargetContainerSlotCode(),
                stockTransferDTO.getSkuBatchStockId()))
                .thenReturn(targetContainerStock);

        // Call the method
        ContainerStock result = stockService.transferContainerStock(stockTransferDTO, containerStock, stockTransferDTO.getSkuBatchStockId(), false);

        // Assertions
        assertEquals(targetContainerStock, result);
        assertEquals(containerStock.getTotalQty(), 0);
        assertEquals(containerStock.getAvailableQty(), 0);
        assertEquals(targetContainerStock.getTotalQty(), 5);
        assertEquals(targetContainerStock.getAvailableQty(), 5);
    }

    @Test
    void testTransferSkuBatchStock() {
        // Setup
        SkuBatchStock skuBatchStock = new SkuBatchStock();
        skuBatchStock.setTotalQty(10);
        skuBatchStock.setAvailableQty(8);
        skuBatchStock.setOutboundLockedQty(2);
        StockTransferDTO stockTransferDTO = new StockTransferDTO();
        stockTransferDTO.setWarehouseAreaId(1L);
        stockTransferDTO.setTransferQty(2);
        stockTransferDTO.setLockType(StockLockTypeEnum.OUTBOUND);

        boolean unlock = true;
        SkuBatchStock targetSkuBatchStock = new SkuBatchStock();

        when(skuBatchStockRepository.findBySkuBatchAttributeIdAndWarehouseAreaId(
                skuBatchStock.getSkuBatchAttributeId(), stockTransferDTO.getWarehouseAreaId())).thenReturn(targetSkuBatchStock);

        // Execute
        SkuBatchStock result = stockService.transferSkuBatchStock(skuBatchStock, stockTransferDTO, unlock);

        // Assertions
        assertEquals(targetSkuBatchStock, result);
        assertEquals(skuBatchStock.getTotalQty(), 8);
        assertEquals(skuBatchStock.getAvailableQty(), 8);
        assertEquals(targetSkuBatchStock.getTotalQty(), 2);
        assertEquals(targetSkuBatchStock.getAvailableQty(), 2);
    }

}
