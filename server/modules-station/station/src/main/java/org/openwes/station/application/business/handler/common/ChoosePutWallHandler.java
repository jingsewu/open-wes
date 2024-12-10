package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChoosePutWallHandler<T extends WorkStationCache> implements IBusinessHandler<String> {

    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationRepository;

    @Override
    public void execute(String body, Long workStationId) {
        T workStation = workStationService.getOrThrow(workStationId);
        if (workStation.getWorkStationMode() == WorkStationModeEnum.PICKING) {
            ((OutboundWorkStationCache) workStation).setActivePutWallCode(body);
            workStationRepository.save(workStation);
        }
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CHOOSE_PUT_WALL;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
