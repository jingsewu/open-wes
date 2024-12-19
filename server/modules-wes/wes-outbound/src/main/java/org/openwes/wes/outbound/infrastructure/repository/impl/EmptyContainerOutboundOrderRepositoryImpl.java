package org.openwes.wes.outbound.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.EmptyContainerOutboundOrderDetailPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.EmptyContainerOutboundOrderPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.EmptyContainerOutboundOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.EmptyContainerOutboundOrderPO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.EmptyContainerOutboundOrderPOTransfer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EmptyContainerOutboundOrderRepositoryImpl implements EmptyContainerOutboundOrderRepository {

    private final EmptyContainerOutboundOrderPORepository orderPORepository;
    private final EmptyContainerOutboundOrderDetailPORepository detailPORepository;
    private final EmptyContainerOutboundOrderPOTransfer emptyContainerOutboundOrderPOTransfer;

    @Override
    public void save(EmptyContainerOutboundOrder order) {

        EmptyContainerOutboundOrderPO saved = orderPORepository.save(emptyContainerOutboundOrderPOTransfer.toPO(order));

        order.getDetails().forEach(detail ->
                detail.setEmptyContainerOutboundOrderId(saved.getId()));

        detailPORepository.saveAll(emptyContainerOutboundOrderPOTransfer.toDetailPOs(order.getDetails()));
    }

    @Override
    public List<EmptyContainerOutboundOrder> findAllByIds(List<Long> orderIds) {

        List<EmptyContainerOutboundOrderPO> orderPOS = orderPORepository.findAllById(orderIds);
        Map<Long, List<EmptyContainerOutboundOrderDetailPO>> orderDetailMap = detailPORepository.findAllByEmptyContainerOutboundOrderIdIn(orderIds)
                .stream().collect(Collectors.groupingBy(EmptyContainerOutboundOrderDetailPO::getEmptyContainerOutboundOrderId));

        return orderPOS.stream().map(orderPO ->
                emptyContainerOutboundOrderPOTransfer.toDO(orderPO, orderDetailMap.get(orderPO.getId()))).toList();

    }

    @Override
    public void saveAll(List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders) {
        orderPORepository.saveAll(emptyContainerOutboundOrderPOTransfer.toPOs(emptyContainerOutboundOrders));
    }

    @Override
    public List<EmptyContainerOutboundOrder> findOrderByDetailIds(List<Long> emptyContainerOutboundOrderDetailIds) {
        List<EmptyContainerOutboundOrderDetailPO> detailPOs = detailPORepository.findAllById(emptyContainerOutboundOrderDetailIds);
        return findAllByIds(detailPOs.stream().map(EmptyContainerOutboundOrderDetailPO::getEmptyContainerOutboundOrderId)
                .collect(Collectors.toList()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAllOrderAndDetails(List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders) {
        detailPORepository.saveAll(emptyContainerOutboundOrderPOTransfer.toDetailPOs(emptyContainerOutboundOrders
                .stream().flatMap(v -> v.getDetails().stream()).toList()));
        orderPORepository.saveAll(emptyContainerOutboundOrderPOTransfer.toPOs(emptyContainerOutboundOrders));
    }
}
