package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.PutWallPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.PutWallSlotPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallSlotPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.PutWallPOTransfer;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.PutWallSlotPOTransfer;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PutWallRepositoryImpl implements PutWallRepository {

    private final PutWallPORepository putWallPORepository;
    private final PutWallSlotPORepository putWallSlotPORepository;
    private final PutWallPOTransfer putWallPOTransfer;
    private final PutWallSlotPOTransfer putWallSlotPOTransfer;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public PutWall save(PutWall putWall) {
        PutWallPO putWallPO = putWallPORepository.save(putWallPOTransfer.toPO(putWall));
        putWall.getPutWallSlots().forEach(v -> v.initPutWallSlot(putWallPO.getId(), putWall.getPutWallCode(), putWall.getWorkStationId()));
        putWallSlotPORepository.saveAll(putWallSlotPOTransfer.toPOs(putWall.getPutWallSlots()));
        return putWallPOTransfer.toDO(putWallPO);
    }

    @Override
    public void saveAll(List<PutWall> putWalls, Long workStationId) {
        putWallPORepository.saveAll(putWallPOTransfer.toPOs(putWalls));
    }

    @Override
    public PutWall findById(Long putWallId) {
        PutWallPO putWallPO = putWallPORepository.findById(putWallId).orElseThrow();
        List<PutWallSlotPO> putWallSlots = putWallSlotPORepository.findAllByPutWallId(putWallPO.getId());
        return putWallPOTransfer.toDO(putWallPO, putWallSlots);
    }

    @Override
    public List<PutWall> findAllByWorkStationIds(Collection<Long> workStationIds) {
        List<PutWallPO> putWallPOS = putWallPORepository.findAllByWorkStationIdIn(workStationIds);
        return transferToPutWall(putWallPOS);
    }

    @Override
    public List<PutWall> findAllByWorkStationId(Long workStationId) {
        List<PutWallPO> putWallPOs = putWallPORepository.findAllByWorkStationId(workStationId);
        return transferToPutWall(putWallPOs);
    }

    private List<PutWall> transferToPutWall(List<PutWallPO> putWallPOS) {
        List<Long> putWallIds = putWallPOS.stream().map(PutWallPO::getId).toList();
        Map<Long, List<PutWallSlotPO>> putWallSlotPOGroupMap = putWallSlotPORepository.findAllByPutWallIdIn(putWallIds).stream()
                .collect(Collectors.groupingBy(PutWallSlotPO::getPutWallId));
        return putWallPOS.stream()
                .map(putWallPO -> putWallPOTransfer.toDO(putWallPO, putWallSlotPOGroupMap.get(putWallPO.getId()))).toList();
    }


}
