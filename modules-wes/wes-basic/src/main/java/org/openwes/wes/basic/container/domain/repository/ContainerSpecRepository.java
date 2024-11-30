package org.openwes.wes.basic.container.domain.repository;

import org.openwes.wes.basic.container.domain.entity.ContainerSpec;

import java.util.Collection;
import java.util.List;

public interface ContainerSpecRepository {

    void save(ContainerSpec containerSpec);

    ContainerSpec findByContainerSpecCode(String containerSpecCode, String warehouseCode);

    ContainerSpec findById(Long id);

    List<ContainerSpec> findAllByContainerSpecCodes(Collection<String> containerSpecCodes, String warehouseCode);

    List<ContainerSpec> findAll();

    List<ContainerSpec> findAllByContainerSpecCodeAndWarehouseCode(Collection<String> containerSpecCodes, Collection<String> warehouseCodes);

    void delete(ContainerSpec containerSpec);
}
