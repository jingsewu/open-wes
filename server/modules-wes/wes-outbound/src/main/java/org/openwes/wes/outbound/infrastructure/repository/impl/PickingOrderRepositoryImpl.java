package org.openwes.wes.outbound.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OutboundErrorDescEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.PickingOrderDetailPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.PickingOrderPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderPO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.PickingOrderDetailPOTransfer;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.PickingOrderPOTransfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PickingOrderRepositoryImpl implements PickingOrderRepository {

    private final PickingOrderPORepository pickingOrderPORepository;
    private final PickingOrderDetailPORepository pickingOrderDetailPORepository;
    private final PickingOrderPOTransfer pickingOrderPOTransfer;
    private final PickingOrderDetailPOTransfer pickingOrderDetailPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderAndDetail(PickingOrder pickingOrder) {
        pickingOrder.sendAndClearEvents();

        List<PickingOrderDetail> details = pickingOrder.getDetails().stream().filter(PickingOrderDetail::isModified).toList();
        pickingOrderPORepository.save(pickingOrderPOTransfer.toPO(pickingOrder));
        pickingOrderDetailPORepository.saveAll(pickingOrderDetailPOTransfer.toPOs(details));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(PickingOrder pickingOrder) {
        pickingOrder.sendAndClearEvents();
        pickingOrderPORepository.save(pickingOrderPOTransfer.toPO(pickingOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAllOrders(List<PickingOrder> pickingOrders) {
        pickingOrders.forEach(PickingOrder::sendAndClearEvents);

        pickingOrderPORepository.saveAll(pickingOrderPOTransfer.toPOs(pickingOrders));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PickingOrder> saveAllOrderAndDetails(List<PickingOrder> pickingOrders) {
        pickingOrders.forEach(PickingOrder::sendAndClearEvents);

        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository
                .saveAll(pickingOrderPOTransfer.toPOs(pickingOrders));

        List<PickingOrderDetail> pickingOrderDetails = pickingOrders.stream()
                .flatMap(v -> v.getDetails().stream().filter(PickingOrderDetail::isModified))
                .toList();
        pickingOrderDetailPORepository.saveAll(pickingOrderDetailPOTransfer.toPOs(pickingOrderDetails));
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public PickingOrder findById(Long pickingOrderId) {
        PickingOrderPO pickingOrderPO = pickingOrderPORepository.findById(pickingOrderId)
                .orElseThrow((() -> WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_PICKING_ORDER)));

        return transferPickingOrders(Lists.newArrayList(pickingOrderPO)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findOrderAndDetailsByPickingOrderIds(Collection<Long> pickingOrderIds) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllById(pickingOrderIds);
        return transferPickingOrders(pickingOrderPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findOrderAndDetailsByPickingOrderIdsAndDetailIds(Collection<Long> pickingOrderIds, Collection<Long> detailIds) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllById(pickingOrderIds);
        return transferPickingOrders(pickingOrderPOS, detailIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findByWaveNos(Collection<String> waveNos) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNoIn(waveNos);
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findOrderAndDetailsByWaveNos(Collection<String> waveNos) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNoIn(waveNos);
        return transferPickingOrders(pickingOrderPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findOrderByPickingOrderIds(Collection<Long> pickingOrderIds) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllById(pickingOrderIds);
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    private List<PickingOrder> transferPickingOrders(List<PickingOrderPO> pickingOrderPOS) {
        Map<Long, PickingOrderPO> pickingOrderPOMap = pickingOrderPOS.stream().collect(Collectors.toMap(PickingOrderPO::getId, v -> v));
        Map<Long, List<PickingOrderDetailPO>> pickingOrderDetailMap = pickingOrderDetailPORepository
                .findByPickingOrderIdIn(pickingOrderPOMap.keySet())
                .stream().collect(Collectors.groupingBy(PickingOrderDetailPO::getPickingOrderId));

        List<PickingOrder> pickingOrders = Lists.newArrayList();
        pickingOrderDetailMap.forEach((pickingOrderId, details) ->
                pickingOrders.add(pickingOrderPOTransfer.toDO(pickingOrderPOMap.get(pickingOrderId), details)));

        return pickingOrders;
    }

    private List<PickingOrder> transferPickingOrders(List<PickingOrderPO> pickingOrderPOS, Collection<Long> detailIds) {
        Map<Long, PickingOrderPO> pickingOrderPOMap = pickingOrderPOS.stream().collect(Collectors.toMap(PickingOrderPO::getId, v -> v));
        Map<Long, List<PickingOrderDetailPO>> pickingOrderDetailMap = pickingOrderDetailPORepository.findAllById(detailIds)
                .stream().collect(Collectors.groupingBy(PickingOrderDetailPO::getPickingOrderId));

        List<PickingOrder> pickingOrders = Lists.newArrayList();
        pickingOrderDetailMap.forEach((pickingOrderId, details) ->
                pickingOrders.add(pickingOrderPOTransfer.toDO(pickingOrderPOMap.get(pickingOrderId), details)));

        return pickingOrders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findByWaveNo(String waveNo) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNo(waveNo);
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findWavePickingOrderById(Long pickingOrderId) {
        PickingOrderPO pickingOrderPO = pickingOrderPORepository.findById(pickingOrderId)
                .orElseThrow((() -> WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_PICKING_ORDER)));
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNo(pickingOrderPO.getWaveNo());
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findAllByPickingDetailIds(List<Long> pickingOrderDetailIds) {
        List<PickingOrderDetailPO> pickingOrderDetailPOs = pickingOrderDetailPORepository.findAllById(pickingOrderDetailIds);
        Map<Long, List<PickingOrderDetailPO>> pickingOrderMap = pickingOrderDetailPOs.stream().collect(Collectors.groupingBy(PickingOrderDetailPO::getPickingOrderId));
        List<PickingOrderPO> pickingOrderPOs = pickingOrderPORepository.findAllById(pickingOrderMap.keySet());
        return pickingOrderPOs.stream().map(v -> pickingOrderPOTransfer.toDO(v, pickingOrderMap.get(v.getId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickingOrder> findAllByOutboundPlanOrderId(Long outboundPlanOrderId) {
        List<Long> pickingOrderIds = pickingOrderDetailPORepository.findPickingOrderIdsByOutboundOrderPlanId(outboundPlanOrderId);
        return pickingOrderPOTransfer.toDOs(pickingOrderPORepository.findAllById(pickingOrderIds));
    }

    @Override
    public List<PickingOrder> findAllOrderByOutboundPlanOrderIds(List<Long> outboundPlanOrderIds) {
        List<Long> pickingOrderIds = pickingOrderDetailPORepository.findPickingOrderIdsByOutboundOrderPlanIdIn(outboundPlanOrderIds);
        return pickingOrderPOTransfer.toDOs(pickingOrderPORepository.findAllById(pickingOrderIds));
    }

    @Override
    public List<Long> findAllIdsByStatus(PickingOrderStatusEnum pickingOrderStatusEnum) {
        return pickingOrderPORepository.findAllIdsByPickingOrderStatus(pickingOrderStatusEnum);
    }
}
