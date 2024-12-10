package org.openwes.wes.basic.work_station.infrastructure.persistence.mapper;

import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallSlotPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PutWallSlotPORepository extends JpaRepository<PutWallSlotPO, Long> {

    PutWallSlotPO findByPutWallSlotCodeAndWorkStationId(String putWallSlotCode, Long workStationId);

    List<PutWallSlotPO> findAllByPutWallId(Long putWallId);

    List<PutWallSlotPO> findAllByPutWallIdIn(List<Long> putWallIds);

    List<PutWallSlotPO> findAllByPickingOrderId(Long pickingOrderId);

    List<PutWallSlotPO> findAllByPutWallSlotCodeInAndWorkStationId(Collection<String> putWallSlotCodes, Long workStationId);

    List<PutWallSlotPO> findAllByWorkStationIdIn(List<Long> workStationIds);
}
