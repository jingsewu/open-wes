package org.openwes.station.domain.transfer;

import com.google.common.collect.Lists;
import org.openwes.station.api.dto.InboundWorkStationCacheDTO;
import org.openwes.station.api.dto.OutboundWorkStationCacheDTO;
import org.openwes.station.api.dto.StocktakeWorkStationCacheDTO;
import org.openwes.station.api.dto.WorkStationCacheDTO;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.infrastructure.persistence.po.InboundWorkStationCachePO;
import org.openwes.station.infrastructure.persistence.po.OutboundWorkStationCachePO;
import org.openwes.station.infrastructure.persistence.po.StocktakeWorkStationCachePO;
import org.openwes.station.infrastructure.persistence.po.WorkStationCachePO;
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
public interface WorkStationCacheTransfer {

    InboundWorkStationCachePO toPO(InboundWorkStationCache inboundWorkStationCache);

    InboundWorkStationCache toDO(InboundWorkStationCachePO inboundWorkStationCachePO);

    InboundWorkStationCacheDTO toDTO(InboundWorkStationCache inboundWorkStationCache);

    InboundWorkStationCache toDO(InboundWorkStationCacheDTO inboundWorkStationCacheDto);

    OutboundWorkStationCachePO toPO(OutboundWorkStationCache outboundWorkStationCache);

    OutboundWorkStationCache toDO(OutboundWorkStationCachePO outboundWorkStationCachePO);

    OutboundWorkStationCache toDO(OutboundWorkStationCacheDTO outboundWorkStationCacheDto);

    OutboundWorkStationCacheDTO toDTO(OutboundWorkStationCache outboundWorkStationCache);

    StocktakeWorkStationCachePO toPO(StocktakeWorkStationCache stocktakeWorkStationCache);

    StocktakeWorkStationCache toDO(StocktakeWorkStationCachePO stocktakeWorkStationCachePO);

    StocktakeWorkStationCache toDO(StocktakeWorkStationCacheDTO stocktakeWorkStationCacheDTO);

    StocktakeWorkStationCacheDTO toDTO(StocktakeWorkStationCache stocktakeWorkStationCache);

    WorkStationCachePO toPO(WorkStationCache workStationCache);

    WorkStationCache toDO(WorkStationCachePO workStationCachePO);

    WorkStationCache toDO(WorkStationCacheDTO workStationCacheDto);

    WorkStationCacheDTO toDTO(WorkStationCache workStationCache);


    default <T extends WorkStationCache, S extends WorkStationCachePO> T toGenericDO(S s) {
        if (s instanceof InboundWorkStationCachePO inboundWorkStationCachePO) {
            return (T) toDO(inboundWorkStationCachePO);
        } else if (s instanceof OutboundWorkStationCachePO outboundWorkStationCachePO) {
            return (T) toDO(outboundWorkStationCachePO);
        } else if (s instanceof StocktakeWorkStationCachePO stocktakeWorkStationCachePO) {
            return (T) toDO(stocktakeWorkStationCachePO);
        } else {
            return (T) toDO(s);
        }
    }

    default <T extends WorkStationCache, S extends WorkStationCachePO> List<T> toGenericDOs(List<S> ss) {

        List<T> tt = Lists.newArrayList();
        ss.forEach(s -> {
            T t = toGenericDO(s);
            tt.add(t);
        });
        return tt;
    }

    default <T extends WorkStationCache, S extends WorkStationCachePO> S toGenericPO(T t) {
        if (t instanceof InboundWorkStationCache inboundWorkStationCache) {
            return (S) toPO(inboundWorkStationCache);
        } else if (t instanceof OutboundWorkStationCache inboundWorkStationCache) {
            return (S) toPO(inboundWorkStationCache);
        } else if (t instanceof StocktakeWorkStationCache stocktakeWorkStationCache) {
            return (S) toPO(stocktakeWorkStationCache);
        } else {
            return (S) toPO(t);
        }
    }

    default <T extends WorkStationCache, S extends WorkStationCachePO> List<T> toGenericDOs(Iterable<S> ss) {
        List<T> tt = Lists.newArrayList();
        ss.forEach(s -> {
            T t = toGenericDO(s);
            tt.add(t);
        });
        return tt;
    }

    default <V extends WorkStationCacheDTO, T extends WorkStationCache> V toGenericDTO(T t) {
        if (t instanceof InboundWorkStationCache inboundWorkStationCache) {
            return (V) toDTO(inboundWorkStationCache);
        } else if (t instanceof OutboundWorkStationCache inboundWorkStationCache) {
            return (V) toDTO(inboundWorkStationCache);
        } else if (t instanceof StocktakeWorkStationCache stocktakeWorkStationCache) {
            return (V) toDTO(stocktakeWorkStationCache);
        } else {
            return (V) toDTO(t);
        }
    }

    default <V extends WorkStationCacheDTO, T extends WorkStationCache> T toGenericDO(V v) {
        if (v instanceof InboundWorkStationCacheDTO inboundWorkStationCacheDto) {
            return (T) toDO(inboundWorkStationCacheDto);
        } else if (v instanceof OutboundWorkStationCacheDTO inboundWorkStationCacheDto) {
            return (T) toDO(inboundWorkStationCacheDto);
        } else if (v instanceof StocktakeWorkStationCacheDTO stocktakeWorkStationCacheDto) {
            return (T) toDO(stocktakeWorkStationCacheDto);
        } else {
            return (T) toDO(v);
        }
    }
}

