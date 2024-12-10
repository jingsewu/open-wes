package org.openwes.wes.stock.domain.aggregate;

import com.google.common.collect.Lists;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.ContainerStockLockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.domain.entity.StockAdjustmentDetail;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.domain.repository.StockAbnormalRecordRepository;
import org.openwes.wes.stock.domain.repository.StockAdjustmentRepository;
import org.openwes.wes.stock.domain.transfer.StockAbnormalRecordTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAbnormalAggregate {

    private final IStockApi stockApi;
    private final StockAbnormalRecordTransfer stockAbnormalRecordTransfer;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAbnormalRecordRepository stockAbnormalRecordRepository;
    private final CallbackApiFacade callbackApiFacade;

    @Transactional(rollbackFor = Exception.class)
    public StockAdjustmentOrder createAdjustmentOrder(List<StockAbnormalRecord> stockAbnormalRecords) {
        stockAbnormalRecords.forEach(StockAbnormalRecord::createAdjustmentOrder);
        stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);

        StockAbnormalRecord stockAbnormalRecord = stockAbnormalRecords.iterator().next();
        List<StockAdjustmentDetail> details = stockAbnormalRecords.stream().map(stockAbnormalRecordTransfer::toStockAdjustmentDetail).toList();

        StockAdjustmentOrder stockAdjustmentOrder = new StockAdjustmentOrder(stockAbnormalRecord.getWarehouseCode(), details, "");

        return stockAdjustmentRepository.createOrderAndDetails(stockAdjustmentOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public void upstreamClose(List<StockAbnormalRecord> stockAbnormalRecords) {

        stockAbnormalRecords.forEach(StockAbnormalRecord::upstreamClose);
        stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);

        unlockStock(stockAbnormalRecords);
    }

    @Transactional
    public void manualClose(List<StockAbnormalRecord> stockAbnormalRecords) {
        stockAbnormalRecords.forEach(StockAbnormalRecord::manualClose);
        stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);

        unlockStock(stockAbnormalRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recheckClose(List<StockAbnormalRecord> stockAbnormalRecords, Map<Long, String> idReplayNoMap) {
        stockAbnormalRecords.forEach(v -> {
            if (MapUtils.isEmpty(idReplayNoMap)) return;
            String replayNo = Optional.ofNullable(idReplayNoMap.get(v.getId())).orElse(StringUtils.EMPTY);
            v.recheckClose(replayNo);
        });
        stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);

        unlockStock(stockAbnormalRecords);
    }

    private void unlockStock(List<StockAbnormalRecord> stockAbnormalRecords) {
        List<ContainerStockLockDTO> containerStockLockDTOS = Lists.newArrayList();
        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = Lists.newArrayList();
        stockAbnormalRecords.forEach(v -> {
            containerStockLockDTOS.add(ContainerStockLockDTO.builder().containerStockId(v.getContainerStockId())
                    .taskId(v.getId())
                    .lockQty(-Math.abs(v.getQtyAbnormal()))
                    .lockType(StockLockTypeEnum.STOCK_ABNORMAL).build());
            skuBatchStockLockDTOS.add(SkuBatchStockLockDTO.builder().skuBatchStockId(v.getSkuBatchStockId())
                    .orderDetailId(v.getId())
                    .lockQty(-Math.abs(v.getQtyAbnormal()))
                    .lockType(StockLockTypeEnum.STOCK_ABNORMAL).build());
        });

        if (CollectionUtils.isNotEmpty(skuBatchStockLockDTOS)) {
            stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);
        }
        if (CollectionUtils.isNotEmpty(containerStockLockDTOS)) {
            stockApi.lockContainerStock(containerStockLockDTOS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeAdjustment(List<StockAbnormalRecord> stockAbnormalRecords) {
        stockAbnormalRecords.forEach(StockAbnormalRecord::completeAdjustment);
        stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<StockAbnormalRecord> createStockAbnormalRecords(List<StockAbnormalRecord> stockAbnormalRecords) {

        stockAbnormalRecords.forEach(StockAbnormalRecord::initial);
        List<StockAbnormalRecord> savedStockAbnormalRecords = stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);

        List<ContainerStockLockDTO> containerStockLockDTOS = Lists.newArrayList();
        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = Lists.newArrayList();
        savedStockAbnormalRecords.stream()
                .filter(v -> v.getQtyAbnormal() < 0)
                .forEach(v -> {
                    containerStockLockDTOS.add(ContainerStockLockDTO.builder().containerStockId(v.getContainerStockId())
                            .taskId(v.getId())
                            .lockQty(Math.abs(v.getQtyAbnormal()))
                            .lockType(StockLockTypeEnum.STOCK_ABNORMAL).build());
                    skuBatchStockLockDTOS.add(SkuBatchStockLockDTO.builder().skuBatchStockId(v.getSkuBatchStockId())
                            .orderDetailId(v.getId())
                            .lockQty(Math.abs(v.getQtyAbnormal()))
                            .lockType(StockLockTypeEnum.STOCK_ABNORMAL).build());
                });

        if (CollectionUtils.isNotEmpty(skuBatchStockLockDTOS)) {
            stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);
        }
        if (CollectionUtils.isNotEmpty(containerStockLockDTOS)) {
            stockApi.lockContainerStock(containerStockLockDTOS);
        }

        List<ContainerStockLockDTO> containerStockAddLockDTOS = Lists.newArrayList();
        List<SkuBatchStockLockDTO> skuBatchStockAddLockDTOS = Lists.newArrayList();
        savedStockAbnormalRecords.stream()
                .filter(v -> v.getQtyAbnormal() > 0)
                .forEach(v -> {
                    containerStockAddLockDTOS.add(ContainerStockLockDTO.builder().containerStockId(v.getContainerStockId())
                            .taskId(v.getId())
                            .lockQty(v.getQtyAbnormal())
                            .lockType(StockLockTypeEnum.STOCK_ABNORMAL).build());
                    skuBatchStockAddLockDTOS.add(SkuBatchStockLockDTO.builder().skuBatchStockId(v.getSkuBatchStockId())
                            .orderDetailId(v.getId())
                            .lockQty(v.getQtyAbnormal())
                            .lockType(StockLockTypeEnum.STOCK_ABNORMAL).build());
                });

        if (CollectionUtils.isNotEmpty(skuBatchStockLockDTOS)) {
            stockApi.addAndLockSkuBatchStock(skuBatchStockAddLockDTOS);
        }
        if (CollectionUtils.isNotEmpty(containerStockLockDTOS)) {
            stockApi.addAndLockContainerStock(containerStockAddLockDTOS);
        }

        callbackApiFacade.callback(CallbackApiTypeEnum.STOCK_ABNORMAL_CALLBACK, "", stockAbnormalRecordTransfer.toDTOs(stockAbnormalRecords));
        return savedStockAbnormalRecords;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRecheckOrder(String replayNo, List<StockAbnormalRecord> stockAbnormalRecords) {
        stockAbnormalRecords.forEach(v -> v.createRecheckOrder(replayNo));
        stockAbnormalRecordRepository.saveAll(stockAbnormalRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRecheckOrder(List<StockAbnormalRecord> stockAbnormalRecords) {
        Map<String, List<StockAbnormalRecord>> warehouseAbnormalRecordMap = stockAbnormalRecords.stream()
                .collect(Collectors.groupingBy(StockAbnormalRecord::getWarehouseCode));

        //TODO implement this function;
//        warehouseAbnormalRecordMap.forEach((warehouseCode, records) ->
//                DomainEventPublisher.directSendSyncEvent(
//                        StocktakeDiscrepancyReviewEvent.builder()
//                                .stockAbnormalRecordDTOList(stockAbnormalRecordTransfer.toDTOs(records))
//                                .build()
//                ));
    }
}
