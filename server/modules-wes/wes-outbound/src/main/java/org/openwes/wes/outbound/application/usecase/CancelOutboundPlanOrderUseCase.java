package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelOutboundPlanOrderUseCase {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundPreAllocatedRecordRepository preAllocatedRecordRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final PickingOrderRepository pickingOrderRepository;
    private final IStockApi stockApi;

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<OutboundPlanOrder> outboundPlanOrders,
                        List<OutboundPreAllocatedRecord> preAllocatedRecords,
                        List<OutboundWave> outboundWaves,
                        List<PickingOrder> pickingOrders) {

        outboundPlanOrders.forEach(OutboundPlanOrder::cancel);
        outboundPlanOrderRepository.saveAllOrderAndDetails(outboundPlanOrders);

        // Unlock pre-allocated stock
        if (CollectionUtils.isNotEmpty(preAllocatedRecords)) {
            List<SkuBatchStockLockDTO> unlockDTOs = preAllocatedRecords.stream().map(record -> {
                SkuBatchStockLockDTO dto = new SkuBatchStockLockDTO();
                dto.setSkuBatchStockId(record.getSkuBatchStockId());
                dto.setLockQty(-record.getQtyPreAllocated());
                dto.setLockType(StockLockTypeEnum.OUTBOUND);
                dto.setOrderDetailId(record.getOutboundPlanOrderDetailId());
                return dto;
            }).toList();
            stockApi.lockSkuBatchStock(unlockDTOs);

            preAllocatedRecords.forEach(OutboundPreAllocatedRecord::cancel);
            preAllocatedRecordRepository.saveAll(preAllocatedRecords);
        }

        if (CollectionUtils.isNotEmpty(outboundWaves)) {
            outboundWaves.forEach(OutboundWave::cancel);
            outboundWaveRepository.saveAll(outboundWaves);
        }

        if (CollectionUtils.isNotEmpty(pickingOrders)) {
            pickingOrders.forEach(PickingOrder::cancel);
            pickingOrderRepository.saveAllOrderAndDetails(pickingOrders);
        }
    }
}
