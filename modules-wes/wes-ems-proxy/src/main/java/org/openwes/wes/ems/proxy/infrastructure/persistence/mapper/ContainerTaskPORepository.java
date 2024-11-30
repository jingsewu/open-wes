package org.openwes.wes.ems.proxy.infrastructure.persistence.mapper;

import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContainerTaskPORepository extends JpaRepository<ContainerTaskPO, Long> {

    List<ContainerTaskPO> findAllByTaskCodeIn(Collection<String> taskCodes);
}
