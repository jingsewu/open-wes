package org.openwes.wes.inbound.application;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.InboundErrorDescEnum;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.inbound.IInboundPlanOrderApi;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.common.validator.IValidator;
import org.openwes.wes.common.validator.ValidateResult;
import org.openwes.wes.inbound.domain.aggregate.InboundAcceptAggregate;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;
import org.openwes.wes.inbound.domain.service.InboundPlanOrderService;
import org.openwes.wes.inbound.domain.transfer.InboundPlanOrderTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Primary
@Validated
@RequiredArgsConstructor
@DubboService
public class InboundPlanOrderApiImpl implements IInboundPlanOrderApi {

    private final InboundPlanOrderService inboundPlanOrderService;
    private final InboundPlanOrderTransfer inboundPlanOrderTransfer;
    private final InboundPlanOrderRepository inboundPlanOrderRepository;
    private final AcceptOrderRepository acceptOrderRepository;
    private final DistributeLock distributeLock;
    private final InboundAcceptAggregate inboundAcceptAggregate;

    @Override
    public void createInboundPlanOrder(List<InboundPlanOrderDTO> inboundPlanOrderDTOS) {
        inboundPlanOrderService.beforeDoCreation(inboundPlanOrderDTOS);

        List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderTransfer.toDOs(inboundPlanOrderDTOS);
        inboundPlanOrders.forEach(InboundPlanOrder::initialize);

        ValidateResult<Set<SkuMainDataDTO>> result = inboundPlanOrderService.validateCreation(inboundPlanOrders);
        inboundPlanOrders.forEach(inboundPlanOrder -> inboundPlanOrder.initSkuInfo(result.getResult(IValidator.ValidatorName.SKU)));

        distributeLock.acquireLockIfThrows(RedisConstants.INBOUND_PLAN_ORDER_ADD_LOCK);

        List<InboundPlanOrder> savedOrders;
        try {
            inboundPlanOrderService.syncValidate(inboundPlanOrders);
            savedOrders = inboundPlanOrderRepository.saveAllOrdersAndDetails(inboundPlanOrders);
        } finally {
            distributeLock.releaseLock(RedisConstants.INBOUND_PLAN_ORDER_ADD_LOCK);
        }
        inboundPlanOrderService.afterDoCreation(savedOrders);
    }

    @Override
    public void accept(AcceptRecordDTO acceptRecord) {

        InboundPlanOrder inboundPlanOrder = null;
        if (acceptRecord.getInboundPlanOrderId() != null) {
            inboundPlanOrder = inboundPlanOrderRepository.findById(acceptRecord.getInboundPlanOrderId());
        }

        inboundPlanOrderService.validateAccept(acceptRecord, acceptRecord.getSkuId());

        inboundAcceptAggregate.accept(acceptRecord, inboundPlanOrder);
    }

    @Override
    public void forceCompleteAccept(Long inboundPlanOrderId) {
        InboundPlanOrder inboundPlanOrder = inboundPlanOrderRepository.findById(inboundPlanOrderId);
        inboundPlanOrderService.validateForceCompleteAccept(inboundPlanOrder);

        inboundPlanOrder.forceCompleteAccepted();

        inboundPlanOrderRepository.saveOrderAndDetail(inboundPlanOrder);

    }

    @Override
    public InboundPlanOrderDTO queryByLpnCodeOrCustomerOrderNoAndThrowException(String identifyNo, String
            warehouseCode) {
        List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderRepository.findAllByLpnCodeOrCustomerOrderNoAndWarehouseCode(identifyNo, warehouseCode);

        if (ObjectUtils.isEmpty(inboundPlanOrders)) {
            throw WmsException.throwWmsException(InboundErrorDescEnum.INBOUND_IDENTIFY_NO_NOT_FOUND, identifyNo);
        }

        InboundPlanOrder inboundPlanOrder = inboundPlanOrders.stream().filter(v -> !InboundPlanOrderStatusEnum.isCompleted(v.getInboundPlanOrderStatus()))
                .findFirst().orElseThrow(() -> WmsException.throwWmsException(InboundErrorDescEnum.INBOUND_IDENTIFY_NO_NOT_FOUND, identifyNo));
        return inboundPlanOrderTransfer.toDTO(inboundPlanOrder);
    }

    @Override
    public InboundPlanOrderDTO queryByLpnCodeOrCustomerOrderNo(String identifyNo, String warehouseCode) {
        return null;
    }


    @Override
    public Collection<String> cancel(Collection<String> identifyNos, String warehouseCode) {
        final List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderRepository.findAllByLpnCodeOrCustomerOrderNoAndWarehouseCode(identifyNos, warehouseCode);
        if (CollectionUtils.isEmpty(inboundPlanOrders) || inboundPlanOrders.size() != identifyNos.size()) {
            throw WmsException.throwWmsException(InboundErrorDescEnum.INBOUND_IDENTIFY_NO_NOT_FOUND);
        }
        inboundPlanOrders.forEach(InboundPlanOrder::cancel);
        inboundPlanOrderRepository.saveOrders(inboundPlanOrders);
        return identifyNos;
    }

    @Override
    public Collection<String> close(Set<Long> inboundPlanOrderIds) {
        List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderRepository.findAllByIds(inboundPlanOrderIds);
        if (ObjectUtils.isEmpty(inboundPlanOrders)) {
            return Collections.emptySet();
        }

        inboundPlanOrderService.validateClose(inboundPlanOrders);

        inboundPlanOrders.forEach(InboundPlanOrder::close);
        inboundPlanOrderRepository.saveAllOrdersAndDetails(inboundPlanOrders);
        return inboundPlanOrders.stream().map(InboundPlanOrder::getCustomerOrderNo).collect(Collectors.toSet());
    }

    @Override
    public InboundPlanOrderDTO queryInboundPlanOrderById(Long id) {
        return inboundPlanOrderTransfer.toDTO(inboundPlanOrderRepository.findById(id));
    }

    @Override
    public List<InboundPlanOrderDTO> queryInboundOrderByStationId(Long workStationId) {
        List<AcceptOrderDetail> acceptOrders = acceptOrderRepository.findAllDetailsByStationId(workStationId);
        if (CollectionUtils.isEmpty(acceptOrders)) {
            return new ArrayList<>();
        }
        final Set<Long> inboundPlanOrderDetailIds = acceptOrders.stream().map(AcceptOrderDetail::getInboundPlanOrderDetailId).collect(Collectors.toSet());
        return inboundPlanOrderTransfer.toDTOs(inboundPlanOrderRepository.findAllByDetailIds(inboundPlanOrderDetailIds));
    }

    @Override
    public List<InboundPlanOrderDTO> queryByStatus(Collection<InboundPlanOrderStatusEnum> inboundPlanOrderStatusEnums) {
        return inboundPlanOrderTransfer.toDTOs(inboundPlanOrderRepository.findAllByStatus(inboundPlanOrderStatusEnums));
    }
}
