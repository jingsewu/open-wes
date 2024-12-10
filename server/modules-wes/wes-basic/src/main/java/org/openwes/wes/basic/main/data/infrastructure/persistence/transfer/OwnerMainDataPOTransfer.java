package org.openwes.wes.basic.main.data.infrastructure.persistence.transfer;

import org.openwes.wes.basic.main.data.domain.entity.OwnerMainData;
import org.openwes.wes.basic.main.data.infrastructure.persistence.po.OwnerMainDataPO;
import org.mapstruct.*;

import java.util.Collection;
import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OwnerMainDataPOTransfer {

    @Mapping(target = ".", source = "addressDTO")
    @Mapping(target = ".", source = "contactorDTO")
    OwnerMainDataPO toPO(OwnerMainData ownerMainData);

    @InheritInverseConfiguration
    OwnerMainData toDO(OwnerMainDataPO ownerMainDataPO);

    List<OwnerMainData> toDOs(Collection<OwnerMainDataPO> ownerMainDataPOS);
}
