package org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseAreaPO;
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
public interface WarehouseAreaPOTransfer {

    WarehouseAreaPO toPO(WarehouseArea warehouseArea);

    WarehouseArea toDO(WarehouseAreaPO warehouseAreaPO);

    List<WarehouseArea> toDOs(List<WarehouseAreaPO> warehouseAreaPOS);

    List<WarehouseAreaPO> toPOs(List<WarehouseArea> warehouseAreas);
}
