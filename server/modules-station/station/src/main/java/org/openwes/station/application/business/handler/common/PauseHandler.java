package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PauseHandler implements IBusinessHandler<String> {

    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public void execute(String body, Long workStationId) {
        remoteWorkStationService.pause(workStationId);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.PAUSE;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
