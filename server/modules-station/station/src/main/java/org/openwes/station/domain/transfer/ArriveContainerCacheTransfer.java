package org.openwes.station.domain.transfer;

import org.openwes.station.api.dto.ArrivedContainerCacheDTO;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
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
public interface ArriveContainerCacheTransfer {

    ArrivedContainerCacheDTO toDTO(ArrivedContainerCache arrivedContainerCache);

    ArrivedContainerCache toDO(ArrivedContainerCacheDTO arrivedContainerCacheDto);

    ArrivedContainerCache toDTO(ContainerArrivedEvent.ContainerDetail containerDetail,
                                ContainerArrivedEvent containerArrivedEvent);
}
