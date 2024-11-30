package org.openwes.wes.ems.proxy.infrastructure.persistence.mapper;

import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskAndBusinessTaskRelationPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContainerTaskAndBusinessTaskRelationPORepository extends JpaRepository<ContainerTaskAndBusinessTaskRelationPO, Long> {

    List<ContainerTaskAndBusinessTaskRelationPO> findByTaskIdIn(Collection<Long> taskIds);

    List<ContainerTaskAndBusinessTaskRelationPO> findByCustomerTaskIdIn(Collection<Long> customerTaskIds);
}
