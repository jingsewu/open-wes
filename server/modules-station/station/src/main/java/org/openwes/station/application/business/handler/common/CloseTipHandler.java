package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloseTipHandler implements IBusinessHandler<String> {

    private final WorkStationService workStationService;
    private final WorkStationCacheRepository workStationRepository;

    @Override
    public void execute(String body, Long workStationId) {
        WorkStationCache workStationCache = workStationService.getOrThrow(workStationId);
        workStationCache.closeTip(body);
        workStationRepository.save(workStationCache);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CLOSE_TIP;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
