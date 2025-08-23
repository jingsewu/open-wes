package org.openwes.station.application.business.handler.common.extension.inbound;

import lombok.RequiredArgsConstructor;
import org.openwes.station.application.business.handler.common.ContainerLeaveHandler;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InboundContainerLeaveExtension implements ContainerLeaveHandler.Extension<InboundWorkStationCache> {

    private final WorkStationCacheRepository<InboundWorkStationCache> workStationCacheRepository;

    @Override
    public void doAfterContainerLeave(InboundWorkStationCache workStationCache, String containerCode) {
        workStationCache.completeTasks(containerCode);
        workStationCacheRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.SELECT_CONTAINER_PUT_AWAY;
    }
}
