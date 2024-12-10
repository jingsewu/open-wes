package org.openwes.wes.inbound.infrastructure.repository.impl;

import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;
import org.openwes.wes.inbound.domain.repository.EmptyContainerInboundRepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.EmptyContainerInboundOrderDetailPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.EmptyContainerInboundOrderPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.po.EmptyContainerInboundOrderDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.EmptyContainerInboundOrderPO;
import org.openwes.wes.inbound.infrastructure.persistence.transfer.EmptyContainerInboundOrderPOTransfer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmptyContainerInboundRepositoryImpl implements EmptyContainerInboundRepository {

    @Autowired
    private EmptyContainerInboundOrderPORepository emptyContainerInboundOrderPORepository;

    @Autowired
    private EmptyContainerInboundOrderDetailPORepository emptyContainerInboundOrderDetailPORepository;

    @Autowired
    private EmptyContainerInboundOrderPOTransfer emptyContainerInboundOrderPOTransfer;

    @Override
    @Transactional
    public List<EmptyContainerInboundOrderDetail> saveOrderAndDetails(EmptyContainerInboundOrder emptyContainerInboundOrder) {
        EmptyContainerInboundOrderPO orderPO = emptyContainerInboundOrderPOTransfer.toPO(emptyContainerInboundOrder);
        emptyContainerInboundOrderPORepository.save(orderPO);

        List<EmptyContainerInboundOrderDetailPO> detailPOS = emptyContainerInboundOrderPOTransfer.toDetailPOs(emptyContainerInboundOrder.getDetails());
        for (EmptyContainerInboundOrderDetailPO detailPO : detailPOS) {
            detailPO.setEmptyContainerInboundOrderId(orderPO.getId());
        }
        emptyContainerInboundOrderDetailPORepository.saveAll(detailPOS);
        return emptyContainerInboundOrderPOTransfer.toDetailDOs(detailPOS);
    }

    @Override
    public List<EmptyContainerInboundOrder> findOrdersByDetailIds(Collection<Long> detailIds) {
        List<EmptyContainerInboundOrderDetailPO> detailPOS = emptyContainerInboundOrderDetailPORepository.findAllById(detailIds);
        if (CollectionUtils.isEmpty(detailPOS)) {
            return Collections.emptyList();
        }

        Set<Long> orderIds = detailPOS.stream().map(EmptyContainerInboundOrderDetailPO::getEmptyContainerInboundOrderId).collect(Collectors.toSet());
        List<EmptyContainerInboundOrderPO> orderPOS = emptyContainerInboundOrderPORepository.findAllById(orderIds);

        List<EmptyContainerInboundOrderDetailPO> allDetailPOS = emptyContainerInboundOrderDetailPORepository.findAllByEmptyContainerInboundOrderIdIn(orderIds);
        List<EmptyContainerInboundOrderDetail> allDetails = emptyContainerInboundOrderPOTransfer.toDetailDOs(allDetailPOS);

        Map<Long, List<EmptyContainerInboundOrderDetail>> allDetailMap = allDetails.stream()
            .collect(Collectors.groupingBy(EmptyContainerInboundOrderDetail::getEmptyContainerInboundOrderId));

        List<EmptyContainerInboundOrder> orders = emptyContainerInboundOrderPOTransfer.toDOs(orderPOS);
        for (EmptyContainerInboundOrder order : orders) {
            order.setDetails(allDetailMap.get(order.getId()));
        }

        return orders;
    }


    @Override
    public void saveAllOrderAndDetails(List<EmptyContainerInboundOrder> orders) {

        List<EmptyContainerInboundOrderPO> pos = emptyContainerInboundOrderPOTransfer.toPOs(orders);

        List<EmptyContainerInboundOrderDetail> details = orders.stream().flatMap(v -> v.getDetails().stream()).toList();
        List<EmptyContainerInboundOrderDetailPO> detailPOS = emptyContainerInboundOrderPOTransfer.toDetailPOs(details);

        emptyContainerInboundOrderPORepository.saveAll(pos);
        emptyContainerInboundOrderDetailPORepository.saveAll(detailPOS);
    }
}
