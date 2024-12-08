package org.openwes.wes.stock.domain.aggregate;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.stock.IStockAbnormalRecordApi;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.ContainerStockLockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.api.stock.dto.StockAdjustmentDetailDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.api.stock.event.StockTransferEvent;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.stock.domain.entity.StockAdjustmentDetail;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import org.openwes.wes.stock.domain.transfer.StockAdjustmentDetailTransfer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAdjustmentAggregate {

    private final IStockApi stockApi;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final IStockAbnormalRecordApi stockAbnormalRecordApi;
    private final StockAdjustmentDetailTransfer stockAdjustmentDetailTransfer;
    private final CallbackApiFacade callbackApi;

    @Transactional(rollbackFor = Exception.class)
    public void adjust(List<StockAdjustmentOrder> stockAdjustmentOrders) {
        stockAdjustmentOrders.forEach(StockAdjustmentOrder::adjust);
        stockAdjustmentRepository.saveOrders(stockAdjustmentOrders);

        List<Long> stockAbnormalRecordIds = stockAdjustmentOrders.stream().flatMap(v -> v.getDetails().stream())
                .map(StockAdjustmentDetail::getStockAbnormalRecordId).filter(Objects::nonNull).toList();

        if (CollectionUtils.isNotEmpty(stockAbnormalRecordIds)) {
            stockAbnormalRecordApi.completeAdjustment(stockAbnormalRecordIds);
        }

        List<StockAdjustmentDetailDTO> callbackDetails = Lists.newArrayList();
        for (StockAdjustmentOrder stockAdjustmentOrder : stockAdjustmentOrders) {
            for (StockAdjustmentDetail detail : stockAdjustmentOrder.getDetails()) {

                StockTransferDTO stockTransferDTO = stockAdjustmentDetailTransfer.toStockTransferDTO(detail, stockAdjustmentOrder);

                DomainEventPublisher.sendAsyncDomainEvent(new StockTransferEvent().setStockTransferDTO(stockTransferDTO).setTaskType(OperationTaskTypeEnum.ADJUST));

                StockAdjustmentDetailDTO detailDTO = stockAdjustmentDetailTransfer.toDTO(detail);
                detailDTO.setWarehouseCode(stockAdjustmentOrder.getWarehouseCode());
                callbackDetails.add(detailDTO);
            }
        }

        if (CollectionUtils.isNotEmpty(callbackDetails)) {
            callbackApi.callback(CallbackApiTypeEnum.STOCK_ADJUSTMENT_CALLBACK, "", callbackDetails);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(List<StockAdjustmentOrder> stockAdjustmentOrders) {

        stockAdjustmentOrders.forEach(StockAdjustmentOrder::close);
        stockAdjustmentRepository.saveOrders(stockAdjustmentOrders);

        List<Long> stockAbnormalRecordIds = stockAdjustmentOrders.stream().flatMap(v -> v.getDetails().stream())
                .map(StockAdjustmentDetail::getStockAbnormalRecordId).filter(Objects::nonNull).toList();

        if (CollectionUtils.isNotEmpty(stockAbnormalRecordIds)) {
            stockAbnormalRecordApi.upstreamClose(stockAbnormalRecordIds);
            return;
        }

        List<ContainerStockLockDTO> containerStockLockDTOS = Lists.newArrayList();
        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = Lists.newArrayList();
        stockAdjustmentOrders.stream().flatMap(v -> v.getDetails().stream()).forEach(v -> {
            containerStockLockDTOS.add(ContainerStockLockDTO.builder().containerStockId(v.getContainerStockId())
                    .taskId(v.getId())
                    .lockQty(-v.getQtyAdjustment())
                    .lockType(StockLockTypeEnum.ADJUSTMENT).build());
            skuBatchStockLockDTOS.add(SkuBatchStockLockDTO.builder().skuBatchStockId(v.getSkuBatchStockId())
                    .orderDetailId(v.getId())
                    .lockQty(-v.getQtyAdjustment())
                    .lockType(StockLockTypeEnum.ADJUSTMENT).build());
        });

        if (CollectionUtils.isNotEmpty(skuBatchStockLockDTOS)) {
            stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);
        }
        if (CollectionUtils.isNotEmpty(containerStockLockDTOS)) {
            stockApi.lockContainerStock(containerStockLockDTOS);
        }
    }
}
