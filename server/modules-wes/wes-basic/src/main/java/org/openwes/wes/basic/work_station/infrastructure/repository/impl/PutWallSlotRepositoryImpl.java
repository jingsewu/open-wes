package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.openwes.wes.basic.work_station.domain.repository.PutWallSlotRepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.PutWallSlotPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallSlotPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.PutWallSlotPOTransfer;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.WORK_STATION_PUT_WALL_SLOT_CACHE;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = WORK_STATION_PUT_WALL_SLOT_CACHE)
public class PutWallSlotRepositoryImpl implements PutWallSlotRepository {

    private final PutWallSlotPORepository putWallSlotPORepository;
    private final PutWallSlotPOTransfer putWallSlotPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(key = "'workStation:' + #putWallSlot.workStationId", condition = "#putWallSlot.workStationId != null"),
    })
    public void save(PutWallSlot putWallSlot) {
        putWallSlot.sendAndClearEvents();
        putWallSlotPORepository.save(putWallSlotPOTransfer.toPO(putWallSlot));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "'workStation:' + #workStationId", condition = "#workStationId != null")
    public void saveAll(List<PutWallSlot> putWallSlots, Long workStationId) {
        putWallSlots.forEach(AggregatorRoot::sendAndClearEvents);
        putWallSlotPORepository.saveAll(putWallSlotPOTransfer.toPOs(putWallSlots));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "'workStation:' + #workStationId", condition = "#workStationId != null")
    public void deleteAll(Long workStationId, List<PutWallSlot> deleteSlots) {
        deleteSlots.forEach(AggregatorRoot::sendAndClearEvents);
        putWallSlotPORepository.deleteAll(putWallSlotPOTransfer.toPOs(deleteSlots));
    }

    @Override
    public List<PutWallSlot> findAllByPutWallId(Long putWallId) {
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByPutWallId(putWallId);
        return putWallSlotPOTransfer.toDOs(putWallSlotPOs);
    }

    @Override
    public PutWallSlot findBySlotCodeAndWorkStationId(String putWallSlotCode, Long workStationId) {
        PutWallSlotPO putWallSlotPO = putWallSlotPORepository.findByPutWallSlotCodeAndWorkStationId(putWallSlotCode, workStationId);
        return putWallSlotPOTransfer.toDO(putWallSlotPO);
    }

    @Override
    public List<PutWallSlot> findAllBySlotCodesAndWorkStationId(Collection<String> putWallSlotCodes, Long workStationId) {
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByPutWallSlotCodeInAndWorkStationId(putWallSlotCodes, workStationId);
        return putWallSlotPOTransfer.toDOs(putWallSlotPOs);
    }

    @Override
    public List<PutWallSlot> findAllByPickingOrderId(Long pickingOrderId) {
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByPickingOrderId(pickingOrderId);
        return putWallSlotPOTransfer.toDOs(putWallSlotPOs);
    }

    @Override
    public List<PutWallSlot> findAllByWorkStationIds(Collection<Long> workStationIds) {
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByWorkStationIdIn(workStationIds);
        return putWallSlotPOTransfer.toDOs(putWallSlotPOs);
    }

    @Override
    @Cacheable(key = "'workStation:' + #workStationId")
    public List<PutWallSlot> findAllByWorkStationId(Long workStationId) {
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByWorkStationId(workStationId);
        return putWallSlotPOTransfer.toDOs(putWallSlotPOs);
    }
}
