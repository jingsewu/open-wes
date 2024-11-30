package org.openwes.wes.basic.main.data.infrastructure.persistence.transfer;


import org.openwes.wes.basic.main.data.domain.entity.WarehouseMainData;
import org.openwes.wes.basic.main.data.infrastructure.persistence.po.WarehouseMainDataPO;
import org.mapstruct.*;

import java.util.Collection;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseMainDataPOTransfer {

    @Mapping(target = ".", source = "addressDTO")
    @Mapping(target = ".", source = "contactorDTO")
    WarehouseMainDataPO toPO(WarehouseMainData warehouse);

    @InheritInverseConfiguration
    WarehouseMainData toDO(WarehouseMainDataPO warehouseMainDataPO);

    @InheritInverseConfiguration
    Collection<WarehouseMainData> toDOs(Collection<WarehouseMainDataPO> warehouseMainDataPOs);

}
