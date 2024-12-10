package org.openwes.wes.inbound.application;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.EmptyContainerInboundErrorDescEnum;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.inbound.IEmptyContainerInboundOrderApi;
import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.api.inbound.dto.EmptyContainerInboundRecordDTO;
import org.openwes.wes.inbound.domain.aggregate.EmptyContainerInboundAggregate;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;
import org.openwes.wes.inbound.domain.repository.EmptyContainerInboundRepository;
import org.openwes.wes.inbound.domain.service.EmptyContainerInboundService;
import org.openwes.wes.inbound.domain.transfer.EmptyContainerInboundOrderTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class EmptyContainerInboundOrderApiImpl implements IEmptyContainerInboundOrderApi {

    private final EmptyContainerInboundService emptyContainerInboundService;
    private final EmptyContainerInboundOrderTransfer emptyContainerInboundOrderTransfer;
    private final EmptyContainerInboundAggregate containerInboundAggregate;
    private final EmptyContainerInboundRepository emptyContainerInboundRepository;

    @Override
    public void create(String warehouseCode,
                       EmptyContainerInboundWayEnum inboundWay,
                       List<EmptyContainerInboundRecordDTO> emptyContainerInboundRecordDTOS) {
        List<EmptyContainerInboundOrderDetail> details = emptyContainerInboundOrderTransfer.toDOs(emptyContainerInboundRecordDTOS);
        EmptyContainerInboundOrder emptyContainerInboundOrder
            = emptyContainerInboundService.createOrder(warehouseCode, EmptyContainerInboundWayEnum.SCAN, details);

        List<ContainerDTO> existContainerDTOS = emptyContainerInboundService.validate(emptyContainerInboundOrder);

        // 保存数据并调用 ems 发送搬箱任务
        containerInboundAggregate.create(emptyContainerInboundOrder, existContainerDTOS);
    }

    @Override
    public void completeDetails(Collection<Long> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            throw WmsException.throwWmsException(EmptyContainerInboundErrorDescEnum.CAN_NOT_FIND_EMPTY_CONTAINER_INBOUND_ORDER_DETAIL);
        }

        List<EmptyContainerInboundOrder> orders = emptyContainerInboundRepository.findOrdersByDetailIds(detailIds);

        orders.forEach(order -> {
            order.complete(detailIds);
            //reduce JPA snapshot compare , remove unnecessary data
            order.removeUnCompleteDetails();
        });

        emptyContainerInboundRepository.saveAllOrderAndDetails(orders);
    }
}
