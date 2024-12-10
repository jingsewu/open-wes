package org.openwes.wes.inbound.domain.service;

import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;

import java.util.List;

public interface EmptyContainerInboundService {

    EmptyContainerInboundOrder createOrder(String warehouseCode,
                                           EmptyContainerInboundWayEnum inboundWay,
                                           List<EmptyContainerInboundOrderDetail> details);

    List<ContainerDTO> validate(EmptyContainerInboundOrder emptyContainerInboundOrder);

}
