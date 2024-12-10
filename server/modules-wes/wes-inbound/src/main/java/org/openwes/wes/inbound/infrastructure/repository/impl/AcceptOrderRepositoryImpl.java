package org.openwes.wes.inbound.infrastructure.repository.impl;

import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.AcceptOrderDetailPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.AcceptOrderPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.po.AcceptOrderDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.AcceptOrderPO;
import org.openwes.wes.inbound.infrastructure.persistence.transfer.AcceptOrderPOTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcceptOrderRepositoryImpl implements AcceptOrderRepository {

    private final AcceptOrderPORepository acceptOrderPORepository;
    private final AcceptOrderDetailPORepository acceptOrderDetailPORepository;
    private final AcceptOrderPOTransfer acceptOrderPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderAndDetail(AcceptOrder acceptOrder) {
        AcceptOrderPO acceptOrderPO = acceptOrderPORepository.save(acceptOrderPOTransfer.toPO(acceptOrder));
        List<AcceptOrderDetail> modifiedDetails = acceptOrder.getDetails().stream().filter(AcceptOrderDetail::isModified).toList();
        modifiedDetails.forEach(v -> v.setAcceptOrderId(acceptOrderPO.getId()));
        acceptOrderDetailPORepository.saveAll(acceptOrderPOTransfer.toDetailPOs(modifiedDetails));
    }

    @Override
    public void saveOrder(AcceptOrder acceptOrder) {
        acceptOrderPORepository.save(acceptOrderPOTransfer.toPO(acceptOrder));
    }

    @Override
    public List<AcceptOrder> findAllByInboundPlanOrderIds(Collection<Long> inboundPlanOrderIds) {
        List<AcceptOrderDetailPO> acceptOrderDetailPOS = acceptOrderDetailPORepository.findAllByInboundPlanOrderIdIn(inboundPlanOrderIds);
        Set<Long> acceptOrderIds = acceptOrderDetailPOS.stream().map(AcceptOrderDetailPO::getAcceptOrderId).collect(Collectors.toSet());
        List<AcceptOrderPO> acceptOrderPOs = acceptOrderPORepository.findAllById(acceptOrderIds);
        return acceptOrderPOTransfer.toDOs(acceptOrderPOs);
    }

    @Override
    public List<AcceptOrderDetail> findAllDetailsByStationId(Long workStationId) {
        List<AcceptOrderDetailPO> acceptOrderPOS = acceptOrderDetailPORepository.findAllByWorkStationId(workStationId);
        return acceptOrderPOTransfer.toDetailDOs(acceptOrderPOS);
    }

    @Override
    public AcceptOrder findById(Long acceptOrderId) {

        AcceptOrderPO acceptOrder = acceptOrderPORepository.findById(acceptOrderId).orElseThrow();
        List<AcceptOrderDetailPO> acceptOrderDetails = acceptOrderDetailPORepository.findByAcceptOrderId(acceptOrder.getId());

        return acceptOrderPOTransfer.toDO(acceptOrder, acceptOrderDetails);
    }

    @Override
    public List<AcceptOrder> findAllByIdentifyNo(String identifyNo) {
        List<AcceptOrderPO> acceptOrderPOs = acceptOrderPORepository.findAllByIdentifyNo(identifyNo);
        return acceptOrderPOTransfer.toDOs(acceptOrderPOs);
    }

    @Override
    public AcceptOrder findNewStatusAcceptOrder(String identifyNo) {
        List<AcceptOrderPO> acceptOrderPOs = acceptOrderPORepository.findAllByIdentifyNoAndAcceptOrderStatus(identifyNo, AcceptOrderStatusEnum.NEW);
        if (ObjectUtils.isEmpty(acceptOrderPOs)) {
            return null;
        }

        AcceptOrderPO acceptOrderPO = acceptOrderPOs.get(0);
        List<AcceptOrderDetailPO> acceptOrderDetails = acceptOrderDetailPORepository.findByAcceptOrderId(acceptOrderPO.getId());

        return acceptOrderPOTransfer.toDO(acceptOrderPO, acceptOrderDetails);
    }

}
