package org.openwes.wes.stock.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.stock.domain.aggregate.SkuBatchContainerStockAggregate;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.entity.ContainerStockTransaction;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.openwes.wes.stock.domain.repository.*;
import org.openwes.wes.stock.domain.service.StockService;
import org.openwes.wes.stock.domain.transfer.*;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.persistence.OptimisticLockException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@EnableRetry
@ContextConfiguration(classes = SkuBatchContainerStockAggregateRetryTest.TestConfig.class)
public class SkuBatchContainerStockAggregateRetryTest {

    @Configuration
    static class TestConfig {
        // Aggregate bean (Spring will proxy it for @Retryable)
        @Bean
        public SkuBatchContainerStockAggregate aggregate(
                SkuBatchStockRepository skuBatchStockRepository,
                SkuBatchStockTransfer skuBatchStockTransfer,
                ContainerStockTransactionRepository containerStockTransactionRepository,
                ContainerStockTransactionTransfer containerStockTransactionTransfer,
                ContainerStockTransfer containerStockTransfer,
                ContainerStockRepository containerStockRepository,
                StockService stockService
        ) {
            return new SkuBatchContainerStockAggregate(
                    skuBatchStockRepository,
                    skuBatchStockTransfer,
                    containerStockTransactionRepository,
                    containerStockTransactionTransfer,
                    containerStockTransfer,
                    containerStockRepository,
                    stockService
            );
        }

        // All mocks as beans
        @Bean public SkuBatchStockRepository skuBatchStockRepository() { return mock(SkuBatchStockRepository.class); }
        @Bean public SkuBatchStockTransfer skuBatchStockTransfer() { return mock(SkuBatchStockTransfer.class); }
        @Bean public ContainerStockTransactionRepository containerStockTransactionRepository() { return mock(ContainerStockTransactionRepository.class); }
        @Bean public ContainerStockTransactionTransfer containerStockTransactionTransfer() { return mock(ContainerStockTransactionTransfer.class); }
        @Bean public ContainerStockTransfer containerStockTransfer() { return mock(ContainerStockTransfer.class); }
        @Bean public ContainerStockRepository containerStockRepository() { return mock(ContainerStockRepository.class); }
        @Bean public StockService stockService() { return mock(StockService.class); }
    }

    @Autowired
    private SkuBatchContainerStockAggregate aggregate; // proxied bean

    @Autowired
    private SkuBatchStockRepository skuBatchStockRepository;

    @Autowired
    private ContainerStockTransactionTransfer containerStockTransactionTransfer;

    @Autowired
    private ContainerStockTransfer containerStockTransfer;

    @Autowired
    private ContainerStockRepository containerStockRepository;

    @Test
    void shouldRetryThreeTimesOnOptimisticLock() {
        // Arrange — return real objects from transfer mocks to avoid NPE
        when(containerStockTransactionTransfer.toDO(any()))
                .thenReturn(new ContainerStockTransaction());

        when(containerStockTransfer.toDO(any(),anyLong()))
                .thenReturn(new ContainerStock());

        when(containerStockTransactionTransfer.fromCreateDTOtoDO(any(), any()))
                .thenReturn(new ContainerStockTransaction());

        // Make save throw twice, then succeed
        when(skuBatchStockRepository.save(any()))
                .thenThrow(new OptimisticLockException("first"))
                .thenThrow(new OptimisticLockException("second"))
                .thenReturn(new SkuBatchStock());

        // Act — this will be retried by Spring Retry
        aggregate.createStock(new StockCreateDTO(), 100L);

        // Assert — verify it was retried exactly 3 times
        verify(skuBatchStockRepository, times(3)).save(any());
    }

    private StockCreateDTO stockDTO() {
        // Minimal DTO to avoid NPEs
        StockCreateDTO dto = new StockCreateDTO();
        dto.setSkuBatchAttributeId(1L);
        dto.setWarehouseAreaId(2L);
        dto.setTargetContainerCode("C1");
        dto.setTargetContainerSlotCode("S1");
        dto.setTransferQty(5);
        return dto;
    }

    @Test
    void shouldRetryWhenContainerStockSaveThrows() {
        // Avoid NPE in transfers
        when(containerStockTransactionTransfer.toDO(any()))
                .thenReturn(new ContainerStockTransaction());
        when(containerStockTransfer.toDO(any(), anyLong()))
                .thenReturn(new ContainerStock());
        when(skuBatchStockRepository.findBySkuBatchAttributeIdAndWarehouseAreaId(anyLong(), anyLong()))
                .thenReturn(new SkuBatchStock());
        when(skuBatchStockRepository.save(any()))
                .thenReturn(new SkuBatchStock());
        when(containerStockTransactionTransfer.fromCreateDTOtoDO(any(), any()))
                .thenReturn(new ContainerStockTransaction());

        ContainerStock containerStock = new ContainerStock();
        containerStock.setId(1L);
        containerStock.setTotalQty(10);
        containerStock.setAvailableQty(10);

        when(containerStockRepository.findByContainerAndSlotAndSkuBatch(any(), any(), any())).thenReturn(containerStock);
        doThrow(new OptimisticLockException("first"))
                .doThrow(new OptimisticLockException("second"))
                .doNothing()
                .when(containerStockRepository)
                .save(any(ContainerStock.class));

        aggregate.createStock(stockDTO()  , 200L);

        verify(skuBatchStockRepository, times(3)).save(any());
        verify(containerStockRepository, times(3)).save(any());
    }
}
