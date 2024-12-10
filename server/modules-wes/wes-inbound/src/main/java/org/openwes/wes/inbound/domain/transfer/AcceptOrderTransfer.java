package org.openwes.wes.inbound.domain.transfer;

import org.openwes.wes.api.inbound.dto.AcceptOrderDTO;
import org.openwes.wes.api.inbound.dto.AcceptOrderDetailDTO;
import org.openwes.wes.api.inbound.event.AcceptEvent;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface AcceptOrderTransfer {

    List<AcceptOrderDetailDTO> toDetailDTOs(List<AcceptOrderDetail> acceptOrderDetails);

    List<AcceptOrderDTO> toDTOs(List<AcceptOrder> acceptOrder);

    @Mapping(ignore = true, target = "id")
    AcceptOrderDetail toDetailDO(SkuMainDataDTO skuMainDataDTO, AcceptEvent acceptEvent);

    @Mapping(source = "acceptEvent.targetContainerCode", target = "identifyNo")
    @Mapping(ignore = true, target = "id")
    AcceptOrder toDO(@Valid AcceptEvent acceptEvent);
}
