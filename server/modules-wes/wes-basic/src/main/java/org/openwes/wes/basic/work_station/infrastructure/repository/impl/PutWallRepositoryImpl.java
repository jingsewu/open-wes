package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public PutWall save(PutWall putWall) {
        PutWallPO putWallPO = putWallPORepository.save(putWallPOTransfer.toPO(putWall));
        putWall.getPutWallSlots().forEach(v ->
                v.initPutWallSlot(putWallPO.getId(), putWall.getPutWallCode(), putWall.getWorkStationId()));
        putWallSlotPORepository.saveAll(putWallSlotPOTransfer.toPOs(putWall.getPutWallSlots()));
        return putWallPOTransfer.toDO(putWallPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<PutWall> putWalls) {
        putWallPORepository.saveAll(putWallPOTransfer.toPOs(putWalls));
    }

    @Override
    @Transactional(readOnly = true)
    public PutWall findById(Long putWallId) {
        PutWallPO putWallPO = putWallPORepository.findById(putWallId).orElseThrow();
        List<PutWallSlotPO> putWallSlots = putWallSlotPORepository.findAllByPutWallId(putWallPO.getId());
        return putWallPOTransfer.toDO(putWallPO, putWallSlots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PutWall> findAllByWorkStationIds(Collection<Long> workStationIds) {
        List<PutWallPO> putWallPOs = putWallPORepository.findAllByWorkStationIdIn(workStationIds);
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByWorkStationIdIn(workStationIds);

        Map<Long, List<PutWallSlotPO>> putWallSlotMap = putWallSlotPOs.stream().collect(Collectors.groupingBy(PutWallSlotPO::getPutWallId));

        return putWallPOs.stream().map(v -> putWallPOTransfer.toDO(v, putWallSlotMap.get(v.getId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PutWall> findAllByWorkStationId(Long workStationId) {
        List<PutWallPO> putWallPOs = putWallPORepository.findAllByWorkStationId(workStationId);
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByWorkStationId(workStationId);

        Map<Long, List<PutWallSlotPO>> putWallSlotMap = putWallSlotPOs.stream().collect(Collectors.groupingBy(PutWallSlotPO::getPutWallId));

        return putWallPOs.stream().map(v -> putWallPOTransfer.toDO(v, putWallSlotMap.get(v.getId()))).toList();
    }

    @Override
    public boolean existByContainerSpecCode(String containerSpecCode, String warehouseCode) {
        return putWallPORepository.existsByContainerSpecCodeAndWarehouseCode(containerSpecCode, warehouseCode);
    }

}
