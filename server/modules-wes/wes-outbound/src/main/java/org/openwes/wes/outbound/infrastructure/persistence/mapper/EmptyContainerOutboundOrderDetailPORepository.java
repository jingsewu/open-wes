package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.EmptyContainerOutboundOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmptyContainerOutboundOrderDetailPORepository extends JpaRepository<EmptyContainerOutboundOrderDetailPO, Long> {

    List<EmptyContainerOutboundOrderDetailPO> findAllByEmptyContainerOutboundOrderIdIn(List<Long> orderIds);
}
