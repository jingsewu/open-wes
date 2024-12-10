package org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseLogicPO;
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
public interface WarehouseLogicPOTransfer {

    WarehouseLogicPO toPO(WarehouseLogic warehouseLogic);

    WarehouseLogic toDO(WarehouseLogicPO warehouseLogicPO);

    List<WarehouseLogic> toDOs(List<WarehouseLogicPO> warehouseLogicPOS);

    List<WarehouseLogicPO> toPOs(List<WarehouseLogic> warehouseLogics);
}
