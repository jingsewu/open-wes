package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.inbound.infrastructure.persistence.po.AcceptOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcceptOrderPORepository extends JpaRepository<AcceptOrderPO, Long> {

    List<AcceptOrderPO> findAllByIdentifyNo(String identifyNo);

    List<AcceptOrderPO> findAllByIdentifyNoAndAcceptOrderStatus(String identifyNo, AcceptOrderStatusEnum acceptOrderStatusEnum);
}
