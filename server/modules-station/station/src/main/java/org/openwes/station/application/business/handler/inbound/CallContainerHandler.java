package org.openwes.station.application.business.handler.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.inbound.CallContainerEvent;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.ContainerTaskService;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallContainerHandler implements IBusinessHandler<CallContainerEvent> {

    private final ContainerTaskService containerTaskService;
    private final WorkStationCacheRepository<InboundWorkStationCache> workStationCacheRepository;

    @Override
    public void execute(CallContainerEvent callContainerEvent, Long workStationId) {

        InboundWorkStationCache workStationCache = workStationCacheRepository.findById(workStationId);
        List<ContainerTaskDTO> containerTasks = containerTaskService.createContainerTasks(callContainerEvent.getContainerCodes(), workStationCache);

        workStationCache.saveCallContainers(callContainerEvent, containerTasks);
        workStationCacheRepository.save(workStationCache);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CALL_CONTAINER;
    }

    @Override
    public Class<CallContainerEvent> getParameterClass() {
        return CallContainerEvent.class;
    }

}
