package org.openwes.wes.basic.container.infrastructure.repository.impl;

import org.openwes.wes.basic.container.domain.entity.Container;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import org.openwes.wes.basic.container.infrastructure.persistence.mapper.ContainerPORepository;
import org.openwes.wes.basic.container.infrastructure.persistence.transfer.ContainerPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerRepositoryImpl implements ContainerRepository {

    private final ContainerPORepository containerPORepository;
    private final ContainerPOTransfer containerPOTransfer;

    @Override
    public void save(Container container) {
        containerPORepository.save(containerPOTransfer.toPO(container));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<Container> containers) {
        containerPORepository.saveAll(containerPOTransfer.toPOs(containers));
    }

    @Override
    public Container findById(Long containerId) {
        return containerPOTransfer.toDO(containerPORepository.findById(containerId).orElseThrow());
    }

    @Override
    public Container findByContainerCode(String containerCode, String warehouseCode) {
        return containerPOTransfer.toDO(containerPORepository.findByContainerCodeAndWarehouseCode(containerCode, warehouseCode));
    }

    @Override
    public List<Container> findByContainerCodes(Collection<String> containerCodes, String warehouseCode) {
        return containerPOTransfer.toDOs(containerPORepository.findByContainerCodeInAndWarehouseCode(containerCodes, warehouseCode));
    }

    @Override
    public boolean existByContainerSpecCode(String containerSpecCode, String warehouseCode) {
        return containerPORepository.existsByContainerSpecCodeAndWarehouseCode(containerSpecCode, warehouseCode);
    }


}
