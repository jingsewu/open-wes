package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeHandler implements IBusinessHandler<String> {

    private final RemoteWorkStationService remoteWorkStationService;

    @Override
    public void execute(String body, Long workStationId) {
        remoteWorkStationService.resume(workStationId);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.RESUME;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
