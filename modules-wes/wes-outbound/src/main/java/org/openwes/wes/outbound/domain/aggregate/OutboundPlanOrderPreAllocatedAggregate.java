package org.openwes.wes.outbound.domain.aggregate;

import com.google.common.collect.Lists;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundPlanOrderPreAllocatedAggregate {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundPreAllocatedRecordRepository preAllocatedRecordRepository;
    private final IStockApi stockApi;

    @Transactional(rollbackFor = Exception.class)
    public void preAllocate(OutboundPlanOrder outboundPlanOrder, OutboundAllocateSkuBatchContext preAllocateCache) {

        List<OutboundPreAllocatedRecord> planPreAllocatedRecords = Lists.newArrayList();
        outboundPlanOrder.getDetails().forEach(detail -> {

            List<SkuBatchStockDTO> skuBatchStocks = preAllocateCache.matchSkuBatchStocks(detail.getSkuId(), detail.getOwnerCode(), detail.getBatchAttributes());

            skuBatchStocks = filterDetailWarehouseAreaIds(detail, skuBatchStocks);
            planPreAllocatedRecords.addAll(preAllocate(detail, skuBatchStocks, detail.getOwnerCode()));
        });

        boolean preAllocateResult = outboundPlanOrder.preAllocate(planPreAllocatedRecords);
        outboundPlanOrderRepository.saveOutboundPlanOrder(outboundPlanOrder);

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

    private List<SkuBatchStockDTO> filterDetailWarehouseAreaIds(OutboundPlanOrderDetail detail, List<SkuBatchStockDTO> skuBatchStocks) {
        if (CollectionUtils.isNotEmpty(detail.getWarehouseAreaIds())) {
            skuBatchStocks = skuBatchStocks.stream().filter(k -> detail.getWarehouseAreaIds().contains(k.getWarehouseAreaId())).toList();
        }
        return skuBatchStocks;
    }

    private List<OutboundPreAllocatedRecord> preAllocate(OutboundPlanOrderDetail detail, List<SkuBatchStockDTO> skuBatchStocks, String ownerCode) {

        List<OutboundPreAllocatedRecord> preAllocatedRecords = Lists.newArrayList();

        int qtyRequired = detail.getQtyRequired();
        for (SkuBatchStockDTO skuBatchStockDTO : skuBatchStocks) {
            if (qtyRequired < 1) {
                break;
            }
            int preAllocated = Math.min(skuBatchStockDTO.getAvailableQty(), qtyRequired);
            qtyRequired -= preAllocated;
            skuBatchStockDTO.setAvailableQty(skuBatchStockDTO.getAvailableQty() - qtyRequired);

            OutboundPreAllocatedRecord preAllocatedRecord = new OutboundPreAllocatedRecord()
                    .setOwnerCode(ownerCode)
                    .setSkuBatchStockId(skuBatchStockDTO.getId())
                    .setWarehouseAreaId(skuBatchStockDTO.getWarehouseAreaId())
                    .setSkuId(skuBatchStockDTO.getSkuId())
                    .setBatchAttributes(detail.getBatchAttributes())
                    .setOutboundPlanOrderId(detail.getOutboundPlanOrderId())
                    .setWarehouseAreaIds(detail.getWarehouseAreaIds())
                    .setOutboundPlanOrderDetailId(detail.getId())
                    .setQtyPreAllocated(preAllocated);

            preAllocatedRecords.add(preAllocatedRecord);
        }
        return preAllocatedRecords;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(List<OutboundPreAllocatedRecord> planPreAllocatedRecords) {

        // unlock pre allocate sku batch stock
        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = planPreAllocatedRecords.stream().map(preAllocatedRecord -> {
            SkuBatchStockLockDTO skuBatchStockLockDTO = new SkuBatchStockLockDTO();
            skuBatchStockLockDTO.setSkuBatchStockId(preAllocatedRecord.getSkuBatchStockId());
            skuBatchStockLockDTO.setLockQty(-preAllocatedRecord.getQtyPreAllocated());
            skuBatchStockLockDTO.setLockType(StockLockTypeEnum.OUTBOUND);
            skuBatchStockLockDTO.setOrderDetailId(preAllocatedRecord.getOutboundPlanOrderDetailId());

            return skuBatchStockLockDTO;
        }).toList();
        stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);

        planPreAllocatedRecords.forEach(OutboundPreAllocatedRecord::cancel);
        preAllocatedRecordRepository.saveAll(planPreAllocatedRecords);
    }
}
