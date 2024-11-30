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
public class ChooseAreaHandler<T extends WorkStationCache> implements IBusinessHandler<String> {

    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationRepository;

    @Override
    public void execute(String body, Long workStationId) {
        WorkStationVO.ChooseAreaEnum chooseArea = WorkStationVO.ChooseAreaEnum.valueOf(body);

        T workStation = workStationService.getOrThrow(workStationId);
        workStation.setChooseArea(chooseArea);
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
