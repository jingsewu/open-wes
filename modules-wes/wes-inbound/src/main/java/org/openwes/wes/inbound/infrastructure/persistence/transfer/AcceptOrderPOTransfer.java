package org.openwes.wes.inbound.infrastructure.persistence.transfer;

import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.infrastructure.persistence.po.AcceptOrderDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.AcceptOrderPO;
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
public interface AcceptOrderPOTransfer {
    AcceptOrderPO toPO(AcceptOrder acceptOrder);

    List<AcceptOrder> toDOs(List<AcceptOrderPO> acceptOrderPOS);

    List<AcceptOrderDetailPO> toDetailPOs(List<AcceptOrderDetail> acceptOrderDetails);

    AcceptOrderDetail toDetailDO(AcceptOrderDetailPO acceptOrderDetailPO);

    List<AcceptOrderDetail> toDetailDOs(List<AcceptOrderDetailPO> detailPOS);

    @Mapping(target = "details", source = "acceptOrderDetails")
    AcceptOrder toDO(AcceptOrderPO acceptOrderPO, List<AcceptOrderDetailPO> acceptOrderDetails);

}
