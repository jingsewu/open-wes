package org.openwes.wes.basic.container.domain.repository;

import org.openwes.wes.basic.container.domain.entity.Container;

import java.util.Collection;
import java.util.List;

public interface ContainerRepository {

    void save(Container container);

    void saveAll(List<Container> containers);

    Container findById(Long containerId);

    Container findByContainerCode(String containerCode, String warehouseCode);

    List<Container> findByContainerCodes(Collection<String> containerCodes, String warehouseCode);

    boolean existByContainerSpecCode(String containerSpecCode, String warehouseCode);
}
