package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.inbound.infrastructure.persistence.po.EmptyContainerInboundOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmptyContainerInboundOrderPORepository extends JpaRepository<EmptyContainerInboundOrderPO, Long> {

}
