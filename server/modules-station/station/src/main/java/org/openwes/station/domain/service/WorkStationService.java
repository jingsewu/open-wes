package org.openwes.station.domain.service;

import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.wes.api.basic.dto.WorkStationDTO;

public interface WorkStationService {

    WorkStationCache initWorkStation(Long workStationId);

    WorkStationCache initWorkStation(WorkStationDTO workStationDTO);

    WorkStationCache getWorkStation(Long workStationId);

    WorkStationCache getOrThrow(Long workStationId);
}
