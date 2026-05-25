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
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkStationServiceImpl implements WorkStationService {

    private final WorkStationCacheRepository workStationCacheRepository;
    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public WorkStationCache initWorkStation(Long workStationId) {
        WorkStationDTO workStationDTO = remoteWorkStationService.queryWorkStation(workStationId);
        return initWorkStation(workStationDTO);
    }

    @Override
    public WorkStationCache getWorkStation(Long workStationId) {
        return workStationCacheRepository.findById(workStationId);
    }

    @Override
    public WorkStationCache getOrThrow(Long workStationId) {
        return Optional.ofNullable(getWorkStation(workStationId))
                .orElseThrow(() -> WmsException.throwWmsException(StationErrorDescEnum.STATION_NOT_EXISTS_OR_ALREADY_OFF_LINE));
    }

    @Override
    public WorkStationCache initWorkStation(WorkStationDTO workStationDTO) {
        WorkStationCache workStationCache;
        if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.PICKING
                || workStationDTO.getWorkStationMode() == WorkStationModeEnum.SELECTION) {
            workStationCache = new OutboundWorkStationCache();
        } else if (WorkStationModeEnum.isPutAwayMode(workStationDTO.getWorkStationMode())) {
            workStationCache = new InboundWorkStationCache();
        } else if (workStationDTO.getWorkStationMode() == WorkStationModeEnum.STOCKTAKE) {
            workStationCache = new StocktakeWorkStationCache();
        } else {
            workStationCache = new WorkStationCache();
        }

        workStationCache.setId(workStationDTO.getId());
        workStationCache.setWarehouseCode(workStationDTO.getWarehouseCode());
        workStationCache.setWarehouseAreaId(workStationDTO.getWarehouseAreaId());
        workStationCache.setStationCode(workStationDTO.getStationCode());
        workStationCache.setWorkStationMode(workStationDTO.getWorkStationMode());

        return workStationCache;
    }
}
