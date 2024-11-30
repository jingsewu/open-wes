package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.basic.work_station.domain.entity.WorkStationConfig;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationConfigRepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.WorkStationConfigPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.WorkStationConfigPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.WorkStationConfigPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkStationConfigRepositoryImpl implements WorkStationConfigRepository {

    private final WorkStationConfigPORepository workStationConfigPORepository;
    private final WorkStationConfigPOTransfer workStationConfigPOTransfer;

    @Override
    @CacheEvict(cacheNames = RedisConstants.WORK_STATION_CONFIG_CACHE, key = "#workStationConfig.workStationId")
    public void save(WorkStationConfig workStationConfig) {
        workStationConfigPORepository.save(workStationConfigPOTransfer.toPO(workStationConfig));
    }

    @Override
    @Cacheable(cacheNames = RedisConstants.WORK_STATION_CONFIG_CACHE, key = "#workStationId")
    public WorkStationConfig findByWorkStationId(Long workStationId) {
        WorkStationConfigPO workStationConfigPO = workStationConfigPORepository.findByWorkStationId(workStationId);
        return workStationConfigPOTransfer.toDO(workStationConfigPO);
    }
}
