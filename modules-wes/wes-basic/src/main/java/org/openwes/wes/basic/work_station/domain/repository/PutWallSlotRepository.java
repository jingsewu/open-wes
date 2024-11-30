package org.openwes.wes.basic.work_station.domain.repository;

import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;

import java.util.Collection;
import java.util.List;

public interface PutWallSlotRepository {

    void save(PutWallSlot putWallSlot);

    void saveAll(List<PutWallSlot> putWallSlots);

    void deleteAll(Long putWallId, List<PutWallSlot> deleteSlots);

    PutWallSlot findBySlotCodeAndWorkStationId(String putWallSlotCode, Long workStationId);

    List<PutWallSlot> findAllByPutWallId(Long putWallId);

    List<PutWallSlot> findAllBySlotCodesAndWorkStationId(Collection<String> putWallSlotCodes, Long workStationId);

    List<PutWallSlot> findAllByPickingOrderId(Long pickingOrderId);

    List<PutWallSlot> findAllByWorkStationIds(List<Long> workStationIds);
}
