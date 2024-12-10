package org.openwes.station.application;

import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.station.api.IStationApi;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.dto.TapPtlEvent;
import org.openwes.station.api.dto.WorkStationCacheDTO;
import org.openwes.station.application.executor.HandlerExecutor;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.domain.transfer.WorkStationCacheTransfer;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
public class StationApiImpl<T extends WorkStationCacheDTO, S extends WorkStationCache> implements IStationApi<T> {

    private final HandlerExecutor handlerExecutor;
    private final WorkStationService<S> workStationService;
    private final WorkStationCacheTransfer workStationCacheTransfer;
    private final WorkStationCacheRepository<S> workStationCacheRepository;

    @Override
    public void containerArrive(ContainerArrivedEvent containerArrivedEvent) {
        handlerExecutor.execute(ApiCodeEnum.CONTAINER_ARRIVED, JsonUtils.obj2String(containerArrivedEvent),
                containerArrivedEvent.getWorkStationId());
    }

    @Override
    public void tapPtl(TapPtlEvent tapPtlEvent) {
        handlerExecutor.execute(ApiCodeEnum.TAP_PTL, JsonUtils.obj2String(tapPtlEvent),
                tapPtlEvent.getWorkStationId());
    }

    @Override
    public T getWorkStationCache(Long workStationId) {
        S workStation = workStationService.getWorkStation(workStationId);
        return workStationCacheTransfer.toGenericDTO(workStation);
    }

    @Override
    public void saveWorkStationCache(T workStationCacheDTO) {
        S workStation = workStationCacheTransfer.toGenericDO(workStationCacheDTO);
        workStationCacheRepository.save(workStation);
    }
}
