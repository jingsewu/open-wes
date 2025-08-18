package org.openwes.station.application.business.handler.common;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerLeaveHandler implements IBusinessHandler<String> {

    private final WorkStationCacheRepository<WorkStationCache> workStationCacheRepository;
    private final EquipmentService equipmentService;

    @Override
    public void execute(String containerCode, Long workStationId) {

        WorkStationCache workStationCache = workStationCacheRepository.findById(workStationId);
        List<ArrivedContainerCache> arrivedContainers = workStationCache.clearArrivedContainers(Lists.newArrayList(containerCode));
        workStationCacheRepository.save(workStationCache);

        equipmentService.containerLeave(arrivedContainers, ContainerOperationTypeEnum.LEAVE);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CALL_CONTAINER;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }

}
