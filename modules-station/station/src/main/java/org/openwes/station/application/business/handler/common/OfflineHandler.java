package org.openwes.station.application.business.handler.common;

import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.application.business.handler.common.extension.IExtension;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OfflineHandler<T extends WorkStationCache> implements IBusinessHandler<String> {

    private final RemoteWorkStationService remoteWorkStationService;
    private final WorkStationService<T> workStationService;
    private final WorkStationCacheRepository<T> workStationRepository;
    private final ExtensionFactory extensionFactory;

    @Override
    public void execute(String body, Long workStationId) {

        T workStationCache = workStationService.getOrThrow(workStationId);
        Extension<T> tExtension = extensionFactory.getExtension(workStationCache.getWorkStationMode(), getApiCode());
        if (tExtension != null) {
            tExtension.doBeforeOffline(workStationCache);
        }

        remoteWorkStationService.offline(workStationId);

        workStationCache.setEventCode(getApiCode());
        workStationRepository.delete(workStationCache);
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.OFFLINE;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }

    public interface Extension<T extends WorkStationCache> extends IExtension {

        void doBeforeOffline(T workStationCache);

        default ApiCodeEnum getApiCode() {
            return ApiCodeEnum.OFFLINE;
        }

    }
}
