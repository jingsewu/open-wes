package org.openwes.wes.inbound.domain.service.impl;

import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.common.validator.IValidator;
import org.openwes.wes.common.validator.ValidatorFactory;
import org.openwes.wes.common.validator.impl.ContainerSpecValidator;
import org.openwes.wes.common.validator.impl.EmptyContainerValidator;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;
import org.openwes.wes.inbound.domain.service.EmptyContainerInboundService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmptyContainerInboundServiceImpl implements EmptyContainerInboundService {

    @Override
    public EmptyContainerInboundOrder createOrder(String warehouseCode,
                                                  EmptyContainerInboundWayEnum inboundWay,
                                                  List<EmptyContainerInboundOrderDetail> details) {
        EmptyContainerInboundOrder emptyContainerInboundOrder
                = new EmptyContainerInboundOrder(warehouseCode, inboundWay, details);
        emptyContainerInboundOrder.initial();

        return emptyContainerInboundOrder;
    }

    @Override
    public List<ContainerDTO> validate(EmptyContainerInboundOrder emptyContainerInboundOrder) {
        String warehouseCode = emptyContainerInboundOrder.getWarehouseCode();
        List<EmptyContainerInboundOrderDetail> details = emptyContainerInboundOrder.getDetails();

        List<ContainerSpecValidator.ContainerSpecValidatorObject> specValidatorObjects
                = details.stream().map(detail -> ContainerSpecValidator.ContainerSpecValidatorObject.builder()
                    .containerSpecCode(detail.getContainerSpecCode())
                    .warehouseCode(warehouseCode).build()).toList();

        ContainerSpecValidator containerSpecValidator = (ContainerSpecValidator) ValidatorFactory.getValidator(IValidator.ValidatorName.CONTAINER_SPEC);
        containerSpecValidator.validate(specValidatorObjects);

        List<EmptyContainerValidator.ContainerValidatorObject> stockValidatorObjects
                = details.stream().map(detail -> EmptyContainerValidator.ContainerValidatorObject.builder()
                .containerCode(detail.getContainerCode())
                .warehouseCode(warehouseCode).build()).toList();

        EmptyContainerValidator emptyContainerValidator = (EmptyContainerValidator) ValidatorFactory.getValidator(IValidator.ValidatorName.EMPTY_CONTAINER);
        return emptyContainerValidator.validate(stockValidatorObjects);
    }

}
