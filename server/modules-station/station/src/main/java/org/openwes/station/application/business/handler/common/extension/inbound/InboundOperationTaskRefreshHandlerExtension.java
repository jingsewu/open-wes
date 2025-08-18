package org.openwes.station.application.business.handler.common.extension.inbound;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboundOperationTaskRefreshHandlerExtension
        implements OperationTaskRefreshHandler.Extension<InboundWorkStationCache> {

    private final WorkStationCacheRepository<InboundWorkStationCache> workStationRepository;

    @Override
    public void refresh(InboundWorkStationCache workStationCache) {
        workStationCache.completeTasks();
        workStationRepository.save(workStationCache);
    }

    @Override
    public WorkStationModeEnum getWorkStationMode() {
        return WorkStationModeEnum.SELECT_CONTAINER_PUT_AWAY;
    }

}
