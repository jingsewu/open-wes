package org.openwes.wes.basic.work_station.domain.aggregate;

import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.domain.repository.PutWallSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PutWallAggregate {

    private final PutWallRepository putWallRepository;
    private final PutWallSlotRepository putWallSlotRepository;

    @Transactional(rollbackFor = Exception.class)
    public void save(PutWall putWall) {
        PutWall savePutWall = putWallRepository.save(putWall);
        putWall.getPutWallSlots().forEach(slot -> slot.initPutWallSlot(savePutWall.getId(), putWall.getPutWallCode(), putWall.getWorkStationId()));
        putWallSlotRepository.saveAll(putWall.getPutWallSlots());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(PutWall putWall, List<PutWallSlot> exitSlots) {

        PutWall savePutWall = putWallRepository.save(putWall);
        putWall.getPutWallSlots().forEach(slot -> slot.initPutWallSlot(savePutWall.getId(), putWall.getPutWallCode(), putWall.getWorkStationId()));

        putWallSlotRepository.saveAll(putWall.getPutWallSlots());

        Set<String> putWallSlotCodes = putWall.getPutWallSlots().stream().map(PutWallSlot::getPutWallSlotCode).collect(Collectors.toSet());
        List<PutWallSlot> deleteSlots = exitSlots.stream().filter(slot -> !putWallSlotCodes.contains(slot.getPutWallSlotCode())).toList();

        putWallSlotRepository.deleteAll(putWall.getId(), deleteSlots);
    }
}
