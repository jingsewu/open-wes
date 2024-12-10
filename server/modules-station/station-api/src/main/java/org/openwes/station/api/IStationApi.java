package org.openwes.station.api;

import org.openwes.station.api.dto.TapPtlEvent;
import org.openwes.station.api.dto.WorkStationCacheDTO;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import jakarta.validation.Valid;

public interface IStationApi<T extends WorkStationCacheDTO> {

    void containerArrive(@Valid ContainerArrivedEvent containerArrivedEvent);

    void tapPtl(@Valid TapPtlEvent tapPtlEvent);

    T getWorkStationCache(Long workStationId);

    void saveWorkStationCache(T workStationCacheDTO);
}
