package org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseAreaGroup;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseAreaGroupPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseAreaGroupPOTransfer {

    WarehouseAreaGroupPO toPO(WarehouseAreaGroup warehouseAreaGroup);

    WarehouseAreaGroup toDO(WarehouseAreaGroupPO warehouseAreaGroupPO);
}
