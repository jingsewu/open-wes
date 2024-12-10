package org.openwes.station.application.business.handler.common;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnlineHandler<T extends WorkStationCache> implements IBusinessHandler<String> {

    private final RemoteWorkStationService remoteWorkStationService;
    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationRepository;

    @Override
    public void execute(String body, Long workStationId) {

        WorkStationModeEnum stationMode = WorkStationModeEnum.valueOf(body);
        remoteWorkStationService.online(workStationId, stationMode);

        T workStation = Optional.ofNullable(workStationService.initWorkStation(workStationId))
                .orElseThrow(() -> WmsException.throwWmsException(StationErrorDescEnum.STATION_ONLINE_OPERATION_TYPE_CAN_NOT_BE_NULL));

        workStation.online(stationMode);

        workStationRepository.save(workStation);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.ONLINE;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
