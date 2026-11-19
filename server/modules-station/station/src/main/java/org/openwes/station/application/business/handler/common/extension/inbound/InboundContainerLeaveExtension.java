package org.openwes.station.application.business.handler.common.extension.inbound;

import lombok.RequiredArgsConstructor;
import org.openwes.station.application.business.handler.common.ContainerLeaveHandler;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InboundContainerLeaveExtension implements ContainerLeaveHandler.Extension {

    private final WorkStationCacheRepository workStationCacheRepository;

    @Override
    public void doAfterContainerLeave(WorkStationCache workStationCache, String containerCode) {
        if (workStationCache instanceof InboundWorkStationCache inboundCache) {
            inboundCache.completeTasks(containerCode);
            workStationCacheRepository.save(inboundCache);
        }
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.SELECT_CONTAINER_PUT_AWAY;
    }
}
