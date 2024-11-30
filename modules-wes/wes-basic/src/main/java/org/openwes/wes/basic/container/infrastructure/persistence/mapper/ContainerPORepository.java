package org.openwes.wes.basic.container.infrastructure.persistence.mapper;

import org.openwes.wes.basic.container.infrastructure.persistence.po.ContainerPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContainerPORepository extends JpaRepository<ContainerPO, Long> {

    ContainerPO findByContainerCodeAndWarehouseCode(String containerCode, String warehouseCode);

    List<ContainerPO> findByContainerCodeInAndWarehouseCode(Collection<String> containerCodes, String warehouseCode);

    boolean existsByContainerSpecCodeAndWarehouseCode(String containerSpecCode, String warehouseCode);
}
