package org.openwes.wes.outbound.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderDetailPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PickingOrderDetailPOTransfer {

    PickingOrderDetailPO toPO(PickingOrderDetail pickingOrderDetail);

    List<PickingOrderDetailPO> toPOs(List<PickingOrderDetail> pickingOrderDetails);

    PickingOrderDetail toDO(PickingOrderDetailPO pickingOrderDetailPO);

    List<PickingOrderDetail> toDOs(List<PickingOrderDetailPO> pickingOrderDetailPOS);
}
