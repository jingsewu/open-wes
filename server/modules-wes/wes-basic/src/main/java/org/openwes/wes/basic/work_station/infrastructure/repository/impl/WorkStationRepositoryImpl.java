package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationRepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.WorkStationPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.WorkStationPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.WorkStationPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkStationRepositoryImpl implements WorkStationRepository {

    private final WorkStationPORepository workStationPORepository;
    private final WorkStationPOTransfer workStationPOTransfer;

    @Override
    @Cacheable(cacheNames = RedisConstants.WORK_STATION_CACHE, key = "#id")
    public WorkStation findById(Long id) {
        return workStationPOTransfer.toDO(workStationPORepository.findById(id).orElseThrow());
    }

    @Override
    public List<WorkStation> findAllByWarehouseCode(String warehouseCode) {
        List<WorkStationPO> workStationPOS = workStationPORepository.findAllByWarehouseCode(warehouseCode);
        return workStationPOTransfer.toDOs(workStationPOS);
    }

    @Override
    @CacheEvict(cacheNames = RedisConstants.WORK_STATION_CACHE, key = "#workStation.id", condition = "#workStation.id != null")
    public void save(WorkStation workStation) {
        workStationPORepository.save(workStationPOTransfer.toPO(workStation));
    }
}
