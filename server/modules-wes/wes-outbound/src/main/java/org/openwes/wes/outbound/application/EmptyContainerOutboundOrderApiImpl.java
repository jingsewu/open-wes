package org.openwes.wes.outbound.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.outbound.IEmptyContainerOutboundOrderApi;
import org.openwes.wes.api.outbound.dto.EmptyContainerOutboundOrderCreateDTO;
import org.openwes.wes.outbound.domain.aggregate.EmptyContainerOutboundAggregate;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrderDetail;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Validated
@Service
@Slf4j
public class EmptyContainerOutboundOrderApiImpl implements IEmptyContainerOutboundOrderApi {

    private final IContainerApi containerApi;
    private final EmptyContainerOutboundAggregate emptyContainerOutboundAggregate;
    private final EmptyContainerOutboundOrderRepository emptyContainerOutboundOrderRepository;

    @Override
    public void createEmptyContainerOutboundOrder(EmptyContainerOutboundOrderCreateDTO emptyContainerOutboundOrderCreateDTO) {

        List<ContainerDTO> containerDTOs = containerApi.queryInsideEmptyContainers(emptyContainerOutboundOrderCreateDTO.getContainerSpecCode(), emptyContainerOutboundOrderCreateDTO.getWarehouseCode(),
                emptyContainerOutboundOrderCreateDTO.getWarehouseAreaId());

        if (ObjectUtils.isEmpty(containerDTOs)) {
            throw new IllegalArgumentException("can not found any empty containers");
        }

        if (StringUtils.isNotBlank(emptyContainerOutboundOrderCreateDTO.getWarehouseLogicCode())) {
            containerDTOs = containerDTOs.stream().filter(v -> StringUtils.equals(v.getWarehouseLogicCode(), emptyContainerOutboundOrderCreateDTO.getWarehouseLogicCode())).toList();
        }

        if (Objects.nonNull(emptyContainerOutboundOrderCreateDTO.getEmptySlotNum())) {
            containerDTOs = containerDTOs.stream().filter(v -> Objects.equals(v.getEmptySlotNum(), emptyContainerOutboundOrderCreateDTO.getEmptySlotNum())).toList();
        }

        if (ObjectUtils.isEmpty(containerDTOs)) {
            throw new IllegalArgumentException("can not found any empty containers");
        }

        List<EmptyContainerOutboundOrderDetail> details = containerDTOs.stream().map(v -> {
            EmptyContainerOutboundOrderDetail detail = new EmptyContainerOutboundOrderDetail();
            detail.setContainerId(v.getId());
            detail.setContainerCode(v.getContainerCode());
            return detail;
        }).toList();

        EmptyContainerOutboundOrder order = new EmptyContainerOutboundOrder();
        order.setWarehouseCode(emptyContainerOutboundOrderCreateDTO.getWarehouseCode());
        order.setWarehouseAreaId(emptyContainerOutboundOrderCreateDTO.getWarehouseAreaId());
        order.setActualCount(containerDTOs.size());
        order.setContainerSpecCode(emptyContainerOutboundOrderCreateDTO.getContainerSpecCode());
        order.setDetails(details);
        order.setWorkStationId(emptyContainerOutboundOrderCreateDTO.getWorkStationId());
        order.initial();

        emptyContainerOutboundAggregate.create(order, containerDTOs);

    }

    @Override
    public void execute(List<Long> orderIds) {
        List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders = emptyContainerOutboundOrderRepository.findAllByIds(orderIds);

        emptyContainerOutboundAggregate.execute(emptyContainerOutboundOrders);
    }

}
