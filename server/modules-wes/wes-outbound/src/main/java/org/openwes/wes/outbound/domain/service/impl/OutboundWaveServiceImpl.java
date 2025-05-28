package org.openwes.wes.outbound.domain.service.impl;

import com.google.common.collect.Lists;
import com.open.wes.extension.business.wes.outbound.IOutboundWavePlugin;
import com.open.wes.extension.business.wes.outbound.action.IOutboundWaveSplitAction;
import com.open.wes.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.outbound.domain.entity.*;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.service.OutboundWaveService;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import org.openwes.wes.outbound.domain.transfer.OutboundWaveTransfer;
import org.openwes.wes.outbound.domain.transfer.PickingOrderTransfer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboundWaveServiceImpl implements OutboundWaveService {

    private final PluginSdkUtils pluginUtils;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundWaveTransfer outboundWaveTransfer;
    private final PickingOrderTransfer pickingOrderTransfer;
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer;
    private final OutboundPreAllocatedRecordRepository outboundPreAllocatedRecordRepository;

    @Override
    public Collection<List<OutboundPlanOrder>> wavePickings(List<OutboundPlanOrder> outboundPlanOrders) {

        List<IOutboundWaveWaveAction> outboundWaveActions = pluginUtils.getExtractObject(IOutboundWaveWaveAction.class);
        if (CollectionUtils.isNotEmpty(outboundWaveActions)) {
            return outboundWaveActions.iterator().next().wave(outboundPlanOrderTransfer.toDTOs(outboundPlanOrders))
                    .stream().map(outboundPlanOrderTransfer::toDOs).toList();
        }

        // 外部波次号为空的订单单组独成为一个波次
        List<List<OutboundPlanOrder>> emptyWaveNoOrders = outboundPlanOrders.stream()
                .filter(order -> StringUtils.isEmpty(order.getCustomerWaveNo()))
                .map(List::of).toList();

        // 有波次号的订单按照波次号组波
        Map<String, List<OutboundPlanOrder>> outboundWaveMap = outboundPlanOrders.stream()
                .filter(order -> StringUtils.isNotEmpty(order.getCustomerWaveNo()))
                .collect(Collectors.groupingBy(OutboundPlanOrder::getCustomerWaveNo));

        return CollectionUtils.union(emptyWaveNoOrders, outboundWaveMap.values());
    }

    @Override
    public List<PickingOrder> spiltWave(OutboundWave outboundWave) {

        List<IOutboundWaveSplitAction> outboundWaveSplitActions = pluginUtils.getExtractObject(IOutboundWaveSplitAction.class);
        if (CollectionUtils.isNotEmpty(outboundWaveSplitActions)) {
            List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundWave.getOutboundPlanOrderIds());
            List<PickingOrderDTO> pickingOrderDTOS = outboundWaveSplitActions.iterator().next().split(outboundWaveTransfer.toDTO(outboundWave, outboundPlanOrders));
            return pickingOrderTransfer.toDOs(pickingOrderDTOS);
        }

        Map<Long, List<OutboundPreAllocatedRecord>> warehouseAreaRecordMap = outboundPreAllocatedRecordRepository
                .findByOutboundPlanOrderIds(outboundWave.getOutboundPlanOrderIds())
                .stream().collect(Collectors.groupingBy(OutboundPreAllocatedRecord::getWarehouseAreaId));

        List<PickingOrder> pickingOrders = Lists.newArrayList();
        warehouseAreaRecordMap.forEach((key, value) -> {

            List<PickingOrderDetail> pickingOrderDetails = value.stream()
                    .map(preAllocatedRecord -> {
                                PickingOrderDetail pickingOrderDetail = new PickingOrderDetail()
                                        .setSkuId(preAllocatedRecord.getSkuId())
                                        .setOwnerCode(preAllocatedRecord.getOwnerCode())
                                        .setOutboundOrderPlanId(preAllocatedRecord.getOutboundPlanOrderId())
                                        .setBatchAttributes(preAllocatedRecord.getBatchAttributes())
                                        .setQtyRequired(preAllocatedRecord.getQtyPreAllocated())
                                        .setSkuBatchStockId(preAllocatedRecord.getSkuBatchStockId())
                                        .setOutboundOrderPlanDetailId(preAllocatedRecord.getOutboundPlanOrderDetailId())
                                        .setRetargetingWarehouseAreaIds(preAllocatedRecord.getWarehouseAreaIds());
                                pickingOrderDetail.setModified(true);
                                return pickingOrderDetail;
                            }
                    ).toList();

            PickingOrder pickingOrder = new PickingOrder()
                    .setPriority(outboundWave.getPriority())
                    .setShortOutbound(outboundWave.isShortOutbound())
                    .setWarehouseCode(outboundWave.getWarehouseCode())
                    .setWarehouseAreaId(key)
                    .setPickingOrderNo(OrderNoGenerator.generationPickingOrderNo())
                    .setWaveNo(outboundWave.getWaveNo())
                    .setDetails(pickingOrderDetails);
            pickingOrder.setAllowReceive(true);
            pickingOrders.add(pickingOrder);
        });
        return pickingOrders;
    }
}
