package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.inbound.infrastructure.persistence.po.EmptyContainerInboundOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EmptyContainerInboundOrderDetailPORepository extends JpaRepository<EmptyContainerInboundOrderDetailPO, Long> {

    List<EmptyContainerInboundOrderDetailPO> findAllByEmptyContainerInboundOrderIdIn(Collection<Long> orderIds);
}
