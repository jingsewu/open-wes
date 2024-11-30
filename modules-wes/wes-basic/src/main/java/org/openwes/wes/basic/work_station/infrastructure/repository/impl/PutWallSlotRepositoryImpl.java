package org.openwes.wes.basic.work_station.infrastructure.repository.impl;

import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.openwes.wes.basic.work_station.domain.repository.PutWallSlotRepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.mapper.PutWallSlotPORepository;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallSlotPO;
import org.openwes.wes.basic.work_station.infrastructure.persistence.transfer.PutWallSlotPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PutWallSlotRepositoryImpl implements PutWallSlotRepository {

    private final PutWallSlotPORepository putWallSlotPORepository;
    private final PutWallSlotPOTransfer putWallSlotPOTransfer;

    @Override
    public void save(PutWallSlot putWallSlot) {
        putWallSlotPORepository.save(putWallSlotPOTransfer.toPO(putWallSlot));
    }

    @Override
    public void saveAll(List<PutWallSlot> putWallSlots) {
        putWallSlotPORepository.saveAll(putWallSlotPOTransfer.toPOs(putWallSlots));
    }

    @Override
    public void deleteAll(Long putWallId, List<PutWallSlot> deleteSlots) {
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
    public List<PutWallSlot> findAllByWorkStationIds(List<Long> workStationIds) {
        List<PutWallSlotPO> putWallSlotPOs = putWallSlotPORepository.findAllByWorkStationIdIn(workStationIds);
        return putWallSlotPOTransfer.toDOs(putWallSlotPOs);
    }


}
