package org.openwes.station.application.business.handler.common;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.OnlineEvent;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnlineHandler implements IBusinessHandler<OnlineEvent> {

    private final RemoteWorkStationService remoteWorkStationService;
    private final WorkStationService workStationService;
    private final WorkStationCacheRepository workStationRepository;

    @Override
    public void execute(OnlineEvent onlineEvent, Long workStationId) {

        remoteWorkStationService.online(workStationId, onlineEvent.getWorkStationMode());

        WorkStationDTO workStationDTO = remoteWorkStationService.queryWorkStation(workStationId);

        WorkStationCache workStation = Optional.ofNullable(workStationService.initWorkStation(workStationDTO))
                .orElseThrow(() -> WmsException.throwWmsException(StationErrorDescEnum.STATION_ONLINE_OPERATION_TYPE_CAN_NOT_BE_NULL));

        workStation.online(workStationDTO, onlineEvent);

        workStationRepository.save(workStation);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.ONLINE;
    }

    @Override
    public Class<OnlineEvent> getParameterClass() {
        return OnlineEvent.class;
    }
}
