package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.inbound.infrastructure.persistence.po.PutAwayTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PutAwayTaskPORepository extends JpaRepository<PutAwayTaskPO, Long> {

    List<PutAwayTaskPO> findAllByTaskNoIn(Collection<String> taskNos);
}
