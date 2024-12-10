package org.openwes.wes.basic.container.infrastructure.persistence.mapper;

import org.openwes.wes.basic.container.infrastructure.persistence.po.ContainerSpecPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

import java.util.List;

public interface ContainerSpecPORepository extends JpaRepository<ContainerSpecPO, Long> {
    ContainerSpecPO findByContainerSpecCodeAndWarehouseCode(String containerSpecCode, String warehouseCode);

    List<ContainerSpecPO> findByContainerSpecCodeInAndWarehouseCode(Collection<String> containerSpecCodes, String warehouseCode);

    List<ContainerSpecPO> findAllByContainerSpecCodeInAndWarehouseCodeIn(Collection<String> containerSpecCodes, Collection<String> warehouseCode);

}
