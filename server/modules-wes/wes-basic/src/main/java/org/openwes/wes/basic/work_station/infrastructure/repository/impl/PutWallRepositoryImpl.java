package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.domain.repository.PutWallSlotRepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.PutWallPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.PutWallPOTransfer;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisConstants.WORK_STATION_PUT_WALL_CACHE)
public class PutWallRepositoryImpl implements PutWallRepository {

    private final PutWallPORepository putWallPORepository;
    private final PutWallSlotRepository putWallSlotRepository;
    private final PutWallPOTransfer putWallPOTransfer;

    @Lazy
    private final PutWallRepositoryImpl self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#putWall.workStationId")
    public PutWall save(PutWall putWall) {
        PutWallPO putWallPO = putWallPORepository.save(putWallPOTransfer.toPO(putWall));
        putWall.getPutWallSlots().forEach(v ->
                v.initPutWallSlot(putWallPO.getId(), putWall.getPutWallCode(), putWall.getWorkStationId()));
        putWallSlotRepository.saveAll(putWall.getPutWallSlots(), putWall.getWorkStationId());
        return putWallPOTransfer.toDO(putWallPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#workStationId")
    public void saveAll(List<PutWall> putWalls, Long workStationId) {
        putWallPORepository.saveAll(putWallPOTransfer.toPOs(putWalls));
    }

    @Override
    @Transactional(readOnly = true)
    public PutWall findById(Long putWallId) {
        PutWallPO putWallPO = putWallPORepository.findById(putWallId).orElseThrow();
        List<PutWallSlot> putWallSlots = putWallSlotRepository.findAllByPutWallId(putWallPO.getId());
        return putWallPOTransfer.toDO(putWallPO, putWallSlots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PutWall> findAllByWorkStationIds(Collection<Long> workStationIds) {
        List<PutWallPO> putWallPOs = putWallPORepository.findAllByWorkStationIdIn(workStationIds);
        List<PutWallSlot> putWallSlotPOs = putWallSlotRepository.findAllByWorkStationIds(workStationIds);

        Map<Long, List<PutWallSlot>> putWallSlotMap = putWallSlotPOs.stream().collect(Collectors.groupingBy(PutWallSlot::getPutWallId));

        return putWallPOs.stream().map(v -> putWallPOTransfer.toDO(v, putWallSlotMap.get(v.getId()))).toList();
    }

    @Override
    @Cacheable(key = "#workStationId")
    public List<PutWall> findAllPutWallsByWorkStationId(Long workStationId) {
        List<PutWallPO> putWallPOs = putWallPORepository.findAllByWorkStationId(workStationId);
        return putWallPOTransfer.toDOs(putWallPOs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PutWall> findAllByWorkStationId(Long workStationId) {
        List<PutWall> putWalls = self.findAllPutWallsByWorkStationId(workStationId);
        List<PutWallSlot> putWallSlotPOs = putWallSlotRepository.findAllByWorkStationId(workStationId);
        Map<Long, List<PutWallSlot>> putWallSlotMap = putWallSlotPOs.stream().collect(Collectors.groupingBy(PutWallSlot::getPutWallId));
        putWalls.forEach(v -> v.setPutWallSlots(putWallSlotMap.get(v.getId())));
        return putWalls;
    }

    @Override
    public boolean existByContainerSpecCode(String containerSpecCode, String warehouseCode) {
        return putWallPORepository.existsByContainerSpecCodeAndWarehouseCode(containerSpecCode, warehouseCode);
    }

}
