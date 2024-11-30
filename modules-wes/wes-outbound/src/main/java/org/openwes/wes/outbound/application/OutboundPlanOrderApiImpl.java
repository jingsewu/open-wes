package org.openwes.wes.outbound.application;

import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.common.validator.IValidator;
import org.openwes.wes.common.validator.ValidateResult;
import org.openwes.wes.outbound.domain.aggregate.OutboundPlanOrderAggregate;
import org.openwes.wes.outbound.domain.context.OutboundPlanOrderCancelContext;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.service.OutboundPlanOrderService;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
public class OutboundPlanOrderApiImpl implements IOutboundPlanOrderApi {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundPlanOrderService outboundPlanOrderService;
    private final OutboundPlanOrderAggregate outboundPlanOrderAggregate;
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer;
    private final DistributeLock distributionLock;

    @Override
    public void createOutboundPlanOrder(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {

        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderDTOs.stream().map(outboundPlanOrderDTO -> {

            outboundPlanOrderService.beforeDoCreation(outboundPlanOrderDTO);

            OutboundPlanOrder outboundPlanOrder = outboundPlanOrderTransfer.toDO(outboundPlanOrderDTO);
            outboundPlanOrder.initialize();

            ValidateResult<Set<SkuMainDataDTO>> result = outboundPlanOrderService.validate(outboundPlanOrder);
            outboundPlanOrder.initSkuInfo(result.getResult(IValidator.ValidatorName.SKU));

            return outboundPlanOrder;
        }).toList();

        //TODO add distribute lock to validate repeated customer order no
//        distributionLock.acquireLockIfThrows();
//
//        List<OutboundPlanOrder> exitsOrders = outboundPlanOrderRepository.findByCustomerOrderNo(outboundPlanOrder.getWarehouseCode(), outboundPlanOrder.getCustomerOrderNo());
//        if (CollectionUtils.isNotEmpty(exitsOrders) && exitsOrders.stream()
//                .anyMatch(o -> !OutboundPlanOrderStatusEnum.isFinalStatues(o.getOutboundPlanOrderStatus()))) {
//            throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_ORDER_IS_EXISTING, outboundPlanOrder.getCustomerOrderNo());
//        }

        List<OutboundPlanOrder> savedOrders = outboundPlanOrderRepository.saveAllOrderAndDetails(outboundPlanOrders);

        savedOrders.forEach(outboundPlanOrderService::afterDoCreation);
    }

    @Override
    public List<OutboundPlanOrderDTO> getOutboundPlanOrders(String warehouseCode, Collection<String> customerOrderNos) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findByCustomerOrderNos(warehouseCode, customerOrderNos);
        return outboundPlanOrderTransfer.toDTOs(outboundPlanOrders);
    }

    @Override
    public List<String> cancelOutboundPlanOrder(OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO) {

        OutboundPlanOrderCancelContext outboundPlanOrderCancelContext = outboundPlanOrderService.prepareCancelContext(outboundPlanOrderCancelDTO);
        outboundPlanOrderAggregate.cancel(outboundPlanOrderCancelContext);
        return outboundPlanOrderCancelContext.getOutboundPlanOrders().stream().map(OutboundPlanOrder::getCustomerOrderNo).toList();
    }

    @Override
    public List<OutboundPlanOrderDTO> getByCustomerWaveNos(Collection<String> customerWaveNos) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findByCustomerWaveNos(customerWaveNos);
        return outboundPlanOrderTransfer.toDTOs(outboundPlanOrders);
    }

    @Override
    public List<OutboundPlanOrderDTO> getByIds(Collection<Long> outboundPlanOrderIds) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundPlanOrderIds);
        return outboundPlanOrderTransfer.toDTOs(outboundPlanOrders);
    }
}
