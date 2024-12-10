package org.openwes.station.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.entity.StocktakeWorkStationCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkStationServiceImpl<T extends WorkStationCache> implements WorkStationService<T> {

    private final WorkStationCacheRepository<T> workStationCacheRepository;
    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public T initWorkStation(Long workStationId) {
        WorkStationDTO workStationDTO = remoteWorkStationService.queryWorkStation(workStationId);
        return initWorkStation(workStationDTO);
    }

    @Override
    public T getWorkStation(Long workStationId) {
        return workStationCacheRepository.findById(workStationId);
    }

    @Override
    public T getOrThrow(Long workStationId) {
        return Optional.ofNullable(getWorkStation(workStationId))
                .orElseThrow(() -> WmsException.throwWmsException(StationErrorDescEnum.STATION_NOT_EXISTS_OR_ALREADY_OFF_LINE));
    }

    @Override
    public T initWorkStation(WorkStationDTO workStationDTO) {
        WorkStationCache workStationCache;
        if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.PICKING) {
            workStationCache = new OutboundWorkStationCache();
        } else if (WorkStationModeEnum.isPutAwayMode(workStationDTO.getWorkStationMode())) {
            workStationCache = new InboundWorkStationCache();
        } else if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.STOCKTAKE) {
            workStationCache = new StocktakeWorkStationCache();
        } else {
            workStationCache = new WorkStationCache();
        }

        BeanUtils.copyProperties(workStationDTO, workStationCache);
        workStationCache.setPutWallSlots(workStationDTO.getPutWalls().stream().flatMap(v -> v.getPutWallSlots().stream()).toList());
        return (T) workStationCache;
    }

    @Override
    public void validatePicking(PutWallSlotDTO putWallSlot) {

        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.IDLE) {
            throw new IllegalStateException("put wall slot is idle, please waiting order dispatched");
        }

        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_BINDING) {
            throw new IllegalStateException("put wall slot wait binding, please bound first");
        }

        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_SEAL) {
            throw new IllegalStateException("put wall slot wait sealing, please seal first");
        }
    }

}
