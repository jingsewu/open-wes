package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PickingOrderDetailPORepository extends JpaRepository<PickingOrderDetailPO, Long> {

    List<PickingOrderDetailPO> findByPickingOrderIdIn(Collection<Long> pickingOrderIds);

}
