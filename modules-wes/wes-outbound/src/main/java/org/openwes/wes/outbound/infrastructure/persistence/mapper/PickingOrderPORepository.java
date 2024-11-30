package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PickingOrderPORepository extends JpaRepository<PickingOrderPO, Long> {

    List<PickingOrderPO> findAllByWaveNoIn(Collection<String> waveNos);

    List<PickingOrderPO> findAllByWaveNo(String waveNo);

}
