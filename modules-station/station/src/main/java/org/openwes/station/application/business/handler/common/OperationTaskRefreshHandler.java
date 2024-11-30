package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.application.business.handler.common.extension.IExtension;
import org.openwes.station.application.business.handler.event.OperationTaskRefreshEvent;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.service.WorkStationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationTaskRefreshHandler<T extends WorkStationCache> implements IBusinessHandler<OperationTaskRefreshEvent> {

    private final WorkStationService<T> workStationService;
    private final ExtensionFactory extensionFactory;

    @Override
    public void execute(@Valid OperationTaskRefreshEvent event, Long workStationId) {

        log.info("work station: {} refresh operation tasks event: {}", workStationId, event);

        T workStation = workStationService.getOrThrow(workStationId);
        if (CollectionUtils.isEmpty(workStation.getArrivedContainers())) {
            log.info("work station: {} refresh container tasks but arrived container is empty", workStationId);
            return;
        }

        Optional<ArrivedContainerCache> optional = workStation.getArrivedContainers().stream()
                .filter(v -> event.getContainerCode().equals(v.getContainerCode()) && event.getFace().equals(v.getFace()))
                .findAny();
        if (optional.isEmpty()) {
            log.warn("work station: {} refresh container tasks but container: {} face: {} not found",
                    workStationId, event.getContainerCode(), event.getFace());
            return;
        }

        Extension<T> extension = extensionFactory.getExtension(workStation.getWorkStationMode(), getApiCode());
        if (extension != null) {
            extension.refresh(workStation);
        }
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CONTAINER_REFRESH;
    }

    @Override
    public Class<OperationTaskRefreshEvent> getParameterClass() {
        return OperationTaskRefreshEvent.class;
    }

    public interface Extension<T extends WorkStationCache> extends IExtension {
        void refresh(T workStationCache);

        default ApiCodeEnum getApiCode() {
            return ApiCodeEnum.CONTAINER_REFRESH;
        }
    }
}
