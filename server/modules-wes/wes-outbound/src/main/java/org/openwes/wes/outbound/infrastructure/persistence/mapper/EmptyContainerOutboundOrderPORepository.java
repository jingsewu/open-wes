package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.EmptyContainerOutboundOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmptyContainerOutboundOrderPORepository extends JpaRepository<EmptyContainerOutboundOrderPO, Long> {

}
