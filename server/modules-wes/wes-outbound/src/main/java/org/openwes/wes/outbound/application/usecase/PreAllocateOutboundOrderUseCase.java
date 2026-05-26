package org.openwes.wes.outbound.application.usecase;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.dto.OutboundAllocateSkuBatchContext;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrderDetail;
import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreAllocateOutboundOrderUseCase {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundPreAllocatedRecordRepository preAllocatedRecordRepository;
    private final IStockApi stockApi;
    private final PickingOrderService pickingOrderService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(Long outboundPlanOrderId) {
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(outboundPlanOrderId);
        if (outboundPlanOrder.getOutboundPlanOrderStatus() != OutboundPlanOrderStatusEnum.NEW) {
            log.error("outbound status must be NEW when preparing allocate stocks");
            return;
        }

        List<Long> skuIds = outboundPlanOrder.getDetails()
                .stream().map(OutboundPlanOrderDetail::getSkuId).toList();
        List<String> ownerCodes = outboundPlanOrder.getDetails()
                .stream().map(OutboundPlanOrderDetail::getOwnerCode).distinct().toList();

        OutboundAllocateSkuBatchContext preAllocateCache =
                pickingOrderService.prepareAllocateCache(skuIds, outboundPlanOrder.getWarehouseCode(), ownerCodes);

        List<OutboundPreAllocatedRecord> planPreAllocatedRecords = Lists.newArrayList();
        outboundPlanOrder.getDetails().forEach(detail -> {
            List<SkuBatchStockDTO> skuBatchStocks = preAllocateCache.matchSkuBatchStocks(
                    detail.getSkuId(), detail.getOwnerCode(), detail.getBatchAttributes());
            skuBatchStocks = filterDetailWarehouseAreaIds(detail, skuBatchStocks);
            planPreAllocatedRecords.addAll(preAllocate(detail, skuBatchStocks));
        });

        boolean preAllocateResult = outboundPlanOrder.preAllocate(planPreAllocatedRecords);
        outboundPlanOrderRepository.saveOrderAndDetail(outboundPlanOrder);

        if (!preAllocateResult) {
            return;
        }

        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = planPreAllocatedRecords.stream().map(preAllocatedRecord -> {
            SkuBatchStockLockDTO skuBatchStockLockDTO = new SkuBatchStockLockDTO();
            skuBatchStockLockDTO.setSkuBatchStockId(preAllocatedRecord.getSkuBatchStockId());
            skuBatchStockLockDTO.setLockQty(preAllocatedRecord.getQtyPreAllocated());
            skuBatchStockLockDTO.setLockType(StockLockTypeEnum.OUTBOUND);
            skuBatchStockLockDTO.setOrderDetailId(preAllocatedRecord.getOutboundPlanOrderDetailId());
            return skuBatchStockLockDTO;
        }).toList();
        stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);

        preAllocatedRecordRepository.saveAll(planPreAllocatedRecords);
    }

    private List<SkuBatchStockDTO> filterDetailWarehouseAreaIds(OutboundPlanOrderDetail detail,
                                                                  List<SkuBatchStockDTO> skuBatchStocks) {
        if (CollectionUtils.isNotEmpty(detail.getWarehouseAreaIds())) {
            skuBatchStocks = skuBatchStocks.stream()
                    .filter(k -> detail.getWarehouseAreaIds().contains(k.getWarehouseAreaId())).toList();
        }
        return skuBatchStocks;
    }

    private List<OutboundPreAllocatedRecord> preAllocate(OutboundPlanOrderDetail detail,
                                                          List<SkuBatchStockDTO> skuBatchStocks) {
        List<OutboundPreAllocatedRecord> preAllocatedRecords = Lists.newArrayList();
        int qtyRequired = detail.getQtyRequired();
        for (SkuBatchStockDTO skuBatchStockDTO : skuBatchStocks) {
            if (qtyRequired < 1) {
                break;
            }
            int preAllocated = Math.min(skuBatchStockDTO.getAvailableQty(), qtyRequired);
            skuBatchStockDTO.setAvailableQty(skuBatchStockDTO.getAvailableQty() - preAllocated);
            qtyRequired -= preAllocated;

            OutboundPreAllocatedRecord preAllocatedRecord = OutboundPreAllocatedRecord.builder()
                    .ownerCode(detail.getOwnerCode())
                    .skuBatchStockId(skuBatchStockDTO.getId())
                    .warehouseAreaId(skuBatchStockDTO.getWarehouseAreaId())
                    .skuId(skuBatchStockDTO.getSkuId())
                    .batchAttributes(detail.getBatchAttributes())
                    .outboundPlanOrderId(detail.getOutboundPlanOrderId())
                    .warehouseAreaIds(detail.getWarehouseAreaIds())
                    .outboundPlanOrderDetailId(detail.getId())
                    .qtyPreAllocated(preAllocated)
                    .build();
            preAllocatedRecords.add(preAllocatedRecord);
        }
        return preAllocatedRecords;
    }
}
