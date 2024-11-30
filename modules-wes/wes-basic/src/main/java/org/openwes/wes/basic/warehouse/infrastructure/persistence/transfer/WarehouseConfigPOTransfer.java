package org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseConfig;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseConfigPOTransfer {

    WarehouseConfigPO toPO(WarehouseConfig warehouseConfig);

    WarehouseConfig toDO(WarehouseConfigPO warehouseConfigPO);
}
