package org.openwes.wes.outbound.domain.transfer;

import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PickingOrderTransfer {
    PickingOrder toDO(PickingOrderDTO pickingOrderDTO);

    List<PickingOrder> toDOs(List<PickingOrderDTO> pickingOrderDTOS);

    PickingOrderDTO toDTO(PickingOrder pickingOrder);

    List<PickingOrderDTO> toDTOs(List<PickingOrder> pickingOrders);

    PickingOrderDetail toDetailDO(PickingOrderDTO.PickingOrderDetailDTO pickingOrderDetailDTO);

    PickingOrderDTO.PickingOrderDetailDTO toDetailDTO(PickingOrderDetail pickingOrderDetail);

    List<PickingOrderDTO.PickingOrderDetailDTO> toDetailDTOs(List<PickingOrderDetail> pickingOrderDetails);
}
