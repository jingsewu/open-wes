package org.openwes.wes.outbound.infrastructure.repository.impl;

import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrderDetail;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.OutboundPlanOrderDetailPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.OutboundPlanOrderPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPlanOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPlanOrderPO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.OutboundPlanOrderPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundPlanOrderRepositoryImpl implements OutboundPlanOrderRepository {

    private final OutboundPlanOrderPORepository outboundPlanOrderPORepository;

    private final OutboundPlanOrderDetailPORepository outboundPlanOrderDetailPORepository;

    private final OutboundPlanOrderPOTransfer outboundPlanOrderPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutboundPlanOrder saveOutboundPlanOrder(OutboundPlanOrder outboundPlanOrder) {
        OutboundPlanOrderPO outboundPlanOrderPO = outboundPlanOrderPORepository.save(outboundPlanOrderPOTransfer.toPO(outboundPlanOrder));

        List<OutboundPlanOrderDetailPO> outboundPlanOrderDetailPOS = outboundPlanOrderPOTransfer.toDetailPOs(outboundPlanOrder.getDetails());
        outboundPlanOrderDetailPOS.forEach(v -> v.setOutboundPlanOrderId(outboundPlanOrderPO.getId()));

        List<OutboundPlanOrderDetailPO> details = outboundPlanOrderDetailPORepository.saveAll(outboundPlanOrderDetailPOS);

        return outboundPlanOrderPOTransfer.toDO(outboundPlanOrderPO, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<OutboundPlanOrder> outboundPlanOrders) {
        outboundPlanOrderPORepository.saveAll(outboundPlanOrderPOTransfer.toPOs(outboundPlanOrders));
    }

    @Override
    public OutboundPlanOrder findByOrderNo(String orderNo) {
        OutboundPlanOrderPO outboundPlanOrderPO = outboundPlanOrderPORepository.findByOrderNo(orderNo);
        List<OutboundPlanOrderDetailPO> details = outboundPlanOrderDetailPORepository
                .findAllByOutboundPlanOrderId(outboundPlanOrderPO.getId());
        return outboundPlanOrderPOTransfer.toDO(outboundPlanOrderPO, details);
    }

    @Override
    public List<OutboundPlanOrder> findAllByIds(Collection<Long> orderIds) {

        List<OutboundPlanOrderPO> outboundPlanOrderPOS = outboundPlanOrderPORepository.findAllById(orderIds);
        return outboundPlanOrderPOS.stream().map(outboundPlanOrderPO -> {
            List<OutboundPlanOrderDetailPO> detailPOS = outboundPlanOrderDetailPORepository
                    .findAllByOutboundPlanOrderId(outboundPlanOrderPO.getId());
            return outboundPlanOrderPOTransfer.toDO(outboundPlanOrderPO, detailPOS);
        }).toList();
    }

    @Override
    public List<OutboundPlanOrder> findByCustomerOrderNo(String warehouseCode, String customerOrderNo) {
        List<OutboundPlanOrderPO> outboundPlanOrderPOS = outboundPlanOrderPORepository.findAllByWarehouseCodeAndCustomerOrderNo(warehouseCode, customerOrderNo);
        return outboundPlanOrderPOS.stream().map(outboundPlanOrderPO -> {
            List<OutboundPlanOrderDetailPO> detailPOS = outboundPlanOrderDetailPORepository
                    .findAllByOutboundPlanOrderId(outboundPlanOrderPO.getId());
            return outboundPlanOrderPOTransfer.toDO(outboundPlanOrderPO, detailPOS);
        }).toList();
    }

    @Override
    public List<OutboundPlanOrder> findByCustomerOrderNos(String warehouseCode, Collection<String> customerOrderNos) {
        List<OutboundPlanOrderPO> outboundPlanOrderPOS = outboundPlanOrderPORepository.findAllByWarehouseCodeAndCustomerOrderNoIn(warehouseCode, customerOrderNos);
        return outboundPlanOrderPOS.stream().map(outboundPlanOrderPO -> {
            List<OutboundPlanOrderDetailPO> detailPOS = outboundPlanOrderDetailPORepository
                    .findAllByOutboundPlanOrderId(outboundPlanOrderPO.getId());
            return outboundPlanOrderPOTransfer.toDO(outboundPlanOrderPO, detailPOS);
        }).toList();
    }

    @Override
    public List<OutboundPlanOrder> findByCustomerWaveNos(Collection<String> customerWaveNos) {
        List<OutboundPlanOrderPO> outboundPlanOrderPOS = outboundPlanOrderPORepository.findAllByCustomerWaveNoIn(customerWaveNos);
        return outboundPlanOrderPOTransfer.toDOs((outboundPlanOrderPOS));
    }

    @Override
    public List<OutboundPlanOrder> findByWaveNos(Collection<String> waveNos) {
        List<OutboundPlanOrderPO> outboundPlanOrderPOS = outboundPlanOrderPORepository.findAllByWaveNoIn(waveNos);
        return outboundPlanOrderPOS.stream().map(outboundPlanOrderPO -> {
            List<OutboundPlanOrderDetailPO> detailPOS = outboundPlanOrderDetailPORepository
                    .findAllByOutboundPlanOrderId(outboundPlanOrderPO.getId());
            return outboundPlanOrderPOTransfer.toDO(outboundPlanOrderPO, detailPOS);
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderAndDetails(List<OutboundPlanOrder> outboundPlanOrders) {
        outboundPlanOrderPORepository.saveAll(outboundPlanOrderPOTransfer.toPOs(outboundPlanOrders));
        List<OutboundPlanOrderDetail> outboundPlanOrderDetails = outboundPlanOrders.stream().flatMap(v -> v.getDetails().stream()).toList();
        outboundPlanOrderDetailPORepository.saveAll(outboundPlanOrderPOTransfer.toDetailPOs(outboundPlanOrderDetails));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OutboundPlanOrder> saveAllOrderAndDetails(List<OutboundPlanOrder> outboundPlanOrders) {
        return outboundPlanOrders.stream().map(this::saveOutboundPlanOrder).toList();
    }

    @Override
    public List<OutboundPlanOrder> findAllByStatus(OutboundPlanOrderStatusEnum outboundPlanOrderStatusEnum) {
        List<OutboundPlanOrderPO> outboundPlanOrderPOs = outboundPlanOrderPORepository.findAllByOutboundPlanOrderStatus(outboundPlanOrderStatusEnum);
        return outboundPlanOrderPOTransfer.toDOs(outboundPlanOrderPOs);
    }
}
