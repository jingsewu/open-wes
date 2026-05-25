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
@SuppressWarnings("unchecked")
public class StationApiImpl implements IStationApi<WorkStationCacheDTO> {

    private final HandlerExecutor handlerExecutor;
    private final WorkStationService workStationService;
    private final WorkStationCacheRepository workStationCacheRepository;

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
    public WorkStationCacheDTO getWorkStationCache(Long workStationId) {
        // TODO: Phase 7 will remove WorkStationCacheDTO; for now return a minimal DTO
        WorkStationCache workStation = workStationService.getWorkStation(workStationId);
        if (workStation == null) {
            return null;
        }
        WorkStationCacheDTO dto = new WorkStationCacheDTO();
        dto.setId(workStation.getId());
        dto.setWarehouseCode(workStation.getWarehouseCode());
        dto.setWarehouseAreaId(workStation.getWarehouseAreaId());
        dto.setStationCode(workStation.getStationCode());
        dto.setWorkStationMode(workStation.getWorkStationMode());
        dto.setWorkStationConfig(workStation.getWorkStationConfig());
        dto.setChooseArea(workStation.getChooseArea());
        dto.setEventCode(workStation.getEventCode());
        return dto;
    }

    @Override
    public void saveWorkStationCache(WorkStationCacheDTO workStationCacheDTO) {
        // TODO: Phase 7 will remove WorkStationCacheDTO; for now look up and save the domain entity
        WorkStationCache workStation = workStationCacheRepository.findById(workStationCacheDTO.getId());
        if (workStation != null) {
            workStation.setWorkStationConfig(workStationCacheDTO.getWorkStationConfig());
            workStation.setChooseArea(workStationCacheDTO.getChooseArea());
            workStation.setEventCode(workStationCacheDTO.getEventCode());
            workStationCacheRepository.save(workStation);
        }
    }
}
