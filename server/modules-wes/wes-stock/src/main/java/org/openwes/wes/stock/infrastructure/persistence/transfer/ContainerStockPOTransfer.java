package org.openwes.wes.stock.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.infrastructure.persistence.po.ContainerStockPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContainerStockPOTransfer {

    ContainerStockPO toPO(ContainerStock containerStock);

    @Mapping(source = "createTime", target = "createTime")
    ContainerStock toDO(ContainerStockPO containerStockPO);

    List<ContainerStock> toDOs(List<ContainerStockPO> containerStockPOs);

    List<ContainerStockPO> toPOs(List<ContainerStock> containerStocks);
}
