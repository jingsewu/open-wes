package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.api.vo.WorkStationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChooseAreaHandler implements IBusinessHandler<String> {

    private final WorkStationService workStationService;
    private final WorkStationCacheRepository workStationRepository;

    @Override
    public void execute(String body, Long workStationId) {
        WorkStationVO.ChooseAreaEnum chooseArea = WorkStationVO.ChooseAreaEnum.valueOf(body);

        WorkStationCache workStation = workStationService.getOrThrow(workStationId);
        workStation.chooseArea(chooseArea);
        workStation.setEventCode(getApiCode());
        workStationRepository.save(workStation);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CHOOSE_AREA;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }
}
