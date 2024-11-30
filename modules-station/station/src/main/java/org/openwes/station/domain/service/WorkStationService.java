package org.openwes.station.domain.service;

import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;

public interface WorkStationService<T extends WorkStationCache> {

    T initWorkStation(Long workStationId);

    T getWorkStation(Long workStationId);

    T getOrThrow(Long workStationId);

    T initWorkStation(WorkStationDTO workStationDTO);

    void validatePicking(PutWallSlotDTO putWallSlot);
}
