package org.openwes.wes.inbound.infrastructure.repository.impl;

import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.InboundPlanOrderDetailPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.mapper.InboundPlanOrderPORepository;
import org.openwes.wes.inbound.infrastructure.persistence.po.InboundPlanOrderDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.InboundPlanOrderPO;
import org.openwes.wes.inbound.infrastructure.persistence.transfer.InboundPlanOrderPOTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundPlanOrderRepositoryImpl implements InboundPlanOrderRepository {

    private final InboundPlanOrderPORepository inboundPlanOrderPORepository;
    private final InboundPlanOrderDetailPORepository inboundPlanOrderDetailPORepository;
    private final InboundPlanOrderPOTransfer inboundPlanOrderPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboundPlanOrder saveOrderAndDetail(InboundPlanOrder inboundPlanOrder) {
        InboundPlanOrderPO inboundPlanOrderPO = inboundPlanOrderPORepository.save(inboundPlanOrderPOTransfer.toPO(inboundPlanOrder));

        List<InboundPlanOrderDetailPO> inboundPlanOrderDetailPOS = inboundPlanOrderPOTransfer.toDetailPOs(inboundPlanOrder.getDetails());
        inboundPlanOrderDetailPOS.forEach(v -> v.setInboundPlanOrderId(inboundPlanOrderPO.getId()));
        inboundPlanOrderDetailPORepository.saveAll(inboundPlanOrderDetailPOS);
        return inboundPlanOrderPOTransfer.toDO(inboundPlanOrderPO, inboundPlanOrderDetailPOS);
    }

    @Override
    public void saveOrders(Collection<InboundPlanOrder> inboundPlanOrders) {
        Collection<InboundPlanOrderPO> orderPOs = inboundPlanOrderPOTransfer.toPOs(inboundPlanOrders);
        inboundPlanOrderPOTransfer.toDOs(inboundPlanOrderPORepository.saveAll(orderPOs));
    }

    @Override
    public InboundPlanOrder findById(Long inboundPlanOrderId) {

        InboundPlanOrderPO inboundPlanOrderPO = inboundPlanOrderPORepository.findById(inboundPlanOrderId).orElseThrow();
        List<InboundPlanOrderDetailPO> inboundPlanOrderDetailPOS = inboundPlanOrderDetailPORepository.findByInboundPlanOrderId(inboundPlanOrderId);

        return inboundPlanOrderPOTransfer.toDO(inboundPlanOrderPO, inboundPlanOrderDetailPOS);
    }

    @Override
    public List<InboundPlanOrder> findAllByLpnCodeOrCustomerOrderNoAndWarehouseCode(String identifyNo, String warehouseCode) {
        List<InboundPlanOrderPO> inboundPlanOrderPOs = inboundPlanOrderPORepository.findByCustomerOrderNoAndWarehouseCode(identifyNo, warehouseCode);
        if (ObjectUtils.isEmpty(inboundPlanOrderPOs)) {
            inboundPlanOrderPOs = inboundPlanOrderPORepository.findByLpnCodeAndWarehouseCode(identifyNo, warehouseCode);
        }
        if (CollectionUtils.isEmpty(inboundPlanOrderPOs)) {
            return Collections.emptyList();
        }

        return inboundPlanOrderPOs.stream().map(inboundPlanOrderPO -> {
            List<InboundPlanOrderDetailPO> details = inboundPlanOrderDetailPORepository.findByInboundPlanOrderId(inboundPlanOrderPO.getId());
            return inboundPlanOrderPOTransfer.toDO(inboundPlanOrderPO, details);
        }).toList();

    }

    @Override
    public List<InboundPlanOrder> findAllByLpnCodeOrCustomerOrderNoAndWarehouseCode(Collection<String> identifyNos, String warehouseCode) {
        List<InboundPlanOrderPO> inboundPlanOrderPOList = inboundPlanOrderPORepository.findAllByLpnCodeInOrCustomerOrderNoInAndWarehouseCode(identifyNos, identifyNos, warehouseCode);

        List<InboundPlanOrderDetailPO> planOrderDetailPOS = inboundPlanOrderDetailPORepository.findByInboundPlanOrderIdIn(inboundPlanOrderPOList.stream().map(InboundPlanOrderPO::getId).collect(Collectors.toSet()));

        Map<Long, List<InboundPlanOrderDetailPO>> detailsGroupByOrderId = planOrderDetailPOS.stream()
                .collect(Collectors.groupingBy(InboundPlanOrderDetailPO::getInboundPlanOrderId));

        return inboundPlanOrderPOList.stream().map(k -> inboundPlanOrderPOTransfer.toDO(k, detailsGroupByOrderId.get(k.getId()))).toList();
    }

    @Override
    public boolean existByCustomerOrderNo(Collection<String> customerOrderNos, String warehouseCode) {
        return inboundPlanOrderPORepository.existsByCustomerOrderNoInAndWarehouseCode(customerOrderNos, warehouseCode);
    }

    @Override
    public boolean existByBoxNos(Collection<String> boxNos, String warehouseCode) {
        List<InboundPlanOrderDetailPO> inboundPlanOrderDetailPOS = inboundPlanOrderDetailPORepository.findByBoxNoIn(boxNos);

        List<InboundPlanOrderPO> inboundPlanOrderPOS = inboundPlanOrderPORepository.findAllById(inboundPlanOrderDetailPOS
                .stream().map(InboundPlanOrderDetailPO::getInboundPlanOrderId).toList());
        return inboundPlanOrderPOS.stream().anyMatch(v -> StringUtils.equals(warehouseCode, v.getWarehouseCode()));
    }

    @Override
    public List<InboundPlanOrder> findAllByIds(Collection<Long> ids) {
        Map<Long, List<InboundPlanOrderDetailPO>> planOrderDetailGroupByOrderId = Optional.ofNullable(inboundPlanOrderDetailPORepository.findByInboundPlanOrderIdIn(ids))
                .filter(CollectionUtils::isNotEmpty)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.groupingBy(InboundPlanOrderDetailPO::getInboundPlanOrderId));

        return Optional.of(inboundPlanOrderPORepository.findAllById(ids))
                .filter(CollectionUtils::isNotEmpty)
                .orElseGet(ArrayList::new)
                .stream()
                .map(inboundPlanOrderPO -> inboundPlanOrderPOTransfer.toDO(inboundPlanOrderPO, planOrderDetailGroupByOrderId.get(inboundPlanOrderPO.getId()))).toList();
    }

    @Override
    public Collection<InboundPlanOrder> findAllByStatus(Collection<InboundPlanOrderStatusEnum> inboundPlanOrderStatusEnums) {
        final List<InboundPlanOrderPO> inboundPlanOrderPOS = inboundPlanOrderPORepository.findAllByInboundPlanOrderStatusIn(inboundPlanOrderStatusEnums);
        if (CollectionUtils.isEmpty(inboundPlanOrderPOS)) {
            return Collections.emptySet();
        }

        final Set<Long> inboundOrderIds = inboundPlanOrderPOS.stream()
                .map(InboundPlanOrderPO::getId)
                .collect(Collectors.toSet());

        final List<InboundPlanOrderDetailPO> planOrderDetailPOS = inboundPlanOrderDetailPORepository.findByInboundPlanOrderIdIn(inboundOrderIds);

        final Map<Long, List<InboundPlanOrderDetailPO>> detailPosMap = planOrderDetailPOS.stream()
                .collect(Collectors.groupingBy(InboundPlanOrderDetailPO::getInboundPlanOrderId));

        return inboundPlanOrderPOS.stream()
                .map(k -> inboundPlanOrderPOTransfer.toDO(k, detailPosMap.get(k.getId())))
                .toList();
    }

    @Override
    public List<InboundPlanOrder> findAllByDetailIds(Collection<Long> detailIds) {
        final List<InboundPlanOrderDetailPO> inboundPlanOrderDetailPOS = inboundPlanOrderDetailPORepository.findAllById(detailIds);
        if (CollectionUtils.isEmpty(inboundPlanOrderDetailPOS)) {
            return new ArrayList<>();
        }
        final Map<Long, List<InboundPlanOrderDetailPO>> inboundOrderMapByInboundId = inboundPlanOrderDetailPOS.stream().collect(Collectors.groupingBy(InboundPlanOrderDetailPO::getInboundPlanOrderId));

        return inboundPlanOrderPORepository.findAllById(inboundOrderMapByInboundId.keySet())
                .stream()
                .map(inboundPlanOrderPO -> inboundPlanOrderPOTransfer.toDO(inboundPlanOrderPO, inboundOrderMapByInboundId.get(inboundPlanOrderPO.getId())))
                .toList();
    }

    @Override
    public boolean existByLpnCodeAndStatus(Set<String> lpnCodes, Collection<InboundPlanOrderStatusEnum> statues, String warehouseCode) {
        return inboundPlanOrderPORepository.existsByLpnCodeInAndInboundPlanOrderStatusInAndWarehouseCode(lpnCodes, statues, warehouseCode);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<InboundPlanOrder> saveAllOrdersAndDetails(List<InboundPlanOrder> inboundPlanOrders) {
        return inboundPlanOrders.stream().map(this::saveOrderAndDetail).toList();
    }
}
