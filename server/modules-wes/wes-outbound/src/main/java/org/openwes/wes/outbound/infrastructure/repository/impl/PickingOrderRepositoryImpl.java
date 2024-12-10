package org.openwes.wes.outbound.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OutboundErrorDescEnum;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.PickingOrderDetailPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.PickingOrderPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderPO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.PickingOrderDetailPOTransfer;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.PickingOrderPOTransfer;
import lombok.RequiredArgsConstructor;
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
    public void saveOrderAndDetail(PickingOrder pickingOrder) {
        PickingOrderPO savedPickingOrderPO = pickingOrderPORepository.save(pickingOrderPOTransfer.toPO(pickingOrder));
        pickingOrder.getDetails().forEach(pickingOrderDetail -> pickingOrderDetail.setPickingOrderId(savedPickingOrderPO.getId()));
        pickingOrderDetailPORepository.saveAll(pickingOrderDetailPOTransfer
                .toPOs(pickingOrder.getDetails().stream().filter(PickingOrderDetail::isModified).toList()));
        pickingOrderPOTransfer.toDO(savedPickingOrderPO);
    }

    @Override
    public void saveAllOrders(List<PickingOrder> pickingOrders) {
        pickingOrderPORepository.saveAll(pickingOrderPOTransfer.toPOs(pickingOrders));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PickingOrder> saveOrderAndDetails(List<PickingOrder> pickingOrders) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository
                .saveAll(pickingOrderPOTransfer.toPOs(pickingOrders));

        Map<String, PickingOrderPO> pickingOrderPOMap = pickingOrderPOS.stream()
                .collect(Collectors.toMap(PickingOrderPO::getPickingOrderNo, v -> v));

        pickingOrders.forEach(pickingOrder -> {
            PickingOrderPO pickingOrderPO = pickingOrderPOMap.get(pickingOrder.getPickingOrderNo());
            pickingOrder.getDetails().forEach(pickingOrderDetail -> pickingOrderDetail.setPickingOrderId(pickingOrderPO.getId()));
        });

        List<PickingOrderDetail> pickingOrderDetails = pickingOrders.stream()
                .flatMap(v -> v.getDetails().stream().filter(PickingOrderDetail::isModified))
                .toList();
        pickingOrderDetailPORepository.saveAll(pickingOrderDetailPOTransfer.toPOs(pickingOrderDetails));

        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    public PickingOrder findById(Long pickingOrderId) {
        PickingOrderPO pickingOrderPO = pickingOrderPORepository.findById(pickingOrderId)
                .orElseThrow((() -> WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_PICKING_ORDER)));

        return transferPickingOrders(Lists.newArrayList(pickingOrderPO)).get(0);
    }

    @Override
    public List<PickingOrder> findOrderAndDetailsByPickingOrderIds(Collection<Long> pickingOrderIds) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllById(pickingOrderIds);
        return transferPickingOrders(pickingOrderPOS);
    }

    @Override
    public List<PickingOrder> findOrderAndDetailsByPickingOrderIdsAndDetailIds(Collection<Long> pickingOrderIds, Collection<Long> detailIds) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllById(pickingOrderIds);
        return transferPickingOrders(pickingOrderPOS, detailIds);
    }

    @Override
    public List<PickingOrder> findByWaveNos(Collection<String> waveNos) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNoIn(waveNos);
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    public List<PickingOrder> findOrderAndDetailsByWaveNos(Collection<String> waveNos) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNoIn(waveNos);
        return transferPickingOrders(pickingOrderPOS);
    }

    @Override
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
    public List<PickingOrder> findByWaveNo(String waveNo) {
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNo(waveNo);
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    public List<PickingOrder> findWavePickingOrderById(Long pickingOrderId) {
        PickingOrderPO pickingOrderPO = pickingOrderPORepository.findById(pickingOrderId)
                .orElseThrow((() -> WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_CANNOT_FIND_PICKING_ORDER)));
        List<PickingOrderPO> pickingOrderPOS = pickingOrderPORepository.findAllByWaveNo(pickingOrderPO.getWaveNo());
        return pickingOrderPOTransfer.toDOs(pickingOrderPOS);
    }

    @Override
    public List<PickingOrder> findAllByPickingDetailIds(List<Long> pickingOrderDetailIds) {
        List<PickingOrderDetailPO> pickingOrderDetailPOs = pickingOrderDetailPORepository.findAllById(pickingOrderDetailIds);
        Map<Long, List<PickingOrderDetailPO>> pickingOrderMap = pickingOrderDetailPOs.stream().collect(Collectors.groupingBy(PickingOrderDetailPO::getPickingOrderId));
        List<PickingOrderPO> pickingOrderPOs = pickingOrderPORepository.findAllById(pickingOrderMap.keySet());
        return pickingOrderPOTransfer.toDOs(pickingOrderPOs);
    }
}
